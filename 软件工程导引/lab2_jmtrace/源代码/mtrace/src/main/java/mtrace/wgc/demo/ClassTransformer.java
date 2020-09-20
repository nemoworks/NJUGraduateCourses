package mtrace.wgc.demo;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;




public class ClassTransformer implements ClassFileTransformer {

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace("/", ".");
        if(className.startsWith("java")||className.startsWith("sun")||className.startsWith("jdk")||className.startsWith("mtrace")){
            return null;
        }
        try {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM5,classWriter) {
                @Override
                public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
                    return new MethodVisitor(Opcodes.ASM5, super.visitMethod(i, s, s1, s2, strings)) {
                        @Override
                        public void visitInsn(int i) {
                            switch (i){
                                //*aload o,i
                                case Opcodes.BALOAD://51
                                case Opcodes.CALOAD://52
                                case Opcodes.SALOAD://53
                                case Opcodes.IALOAD://46
                                case Opcodes.LALOAD://47
                                case Opcodes.FALOAD://48
                                case Opcodes.DALOAD://49
                                case Opcodes.AALOAD://50
                                    mv.visitInsn(Opcodes.DUP2);
                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mtrace/wgc/demo/MTrace", "traceArrayRead", "(Ljava/lang/Object;I)V", false);
                                    break;
                                //*astore o,i,j
                                case Opcodes.BASTORE://84 byte/boolean
                                case Opcodes.CASTORE://85 char
                                case Opcodes.SASTORE://86 short
                                case Opcodes.AASTORE://83 reference**
                                case Opcodes.IASTORE://79 int
                                case Opcodes.FASTORE://81 float
                                    //stack top: o,i,j
                                    mv.visitInsn(Opcodes.DUP_X2);
                                    //stack top: j,o,i,j
                                    mv.visitInsn(Opcodes.POP);
                                    //stack top: j,o,i
                                    mv.visitInsn(Opcodes.DUP2_X1);
                                    //stack top: o,i,j,o,i
                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mtrace/wgc/demo/MTrace", "traceArrayWrite", "(Ljava/lang/Object;I)V", false);
                                    break;
                                //*astore o,i,w
                                case Opcodes.LASTORE://80
                                case Opcodes.DASTORE://82
                                    //stack top: o,i,w
                                    mv.visitInsn(Opcodes.DUP2_X2);
                                    //stack top: w,o,i,w
                                    mv.visitInsn(Opcodes.POP2);
                                    //stack top: w,o,i
                                    mv.visitInsn(Opcodes.DUP2_X2);
                                    //stack top: o,i,w,o,i
                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mtrace/wgc/demo/MTrace", "traceArrayWrite", "(Ljava/lang/Object;I)V", false);
                                    break;
                                default:
                                    break;
                            }
                            super.visitInsn(i);
                        }

                        @Override
                        public void visitFieldInsn(int i, String s, String s1, String s2) {
                            switch (i){
                                case Opcodes.GETSTATIC:
                                case Opcodes.GETFIELD:
                                    mv.visitLdcInsn(s);
                                    mv.visitLdcInsn(s1);
                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mtrace/wgc/demo/MTrace", "traceVarRead", "(Ljava/lang/String;Ljava/lang/String;)V", false);
                                    break;
                                case Opcodes.PUTSTATIC:
                                case Opcodes.PUTFIELD:
                                    mv.visitLdcInsn(s);
                                    mv.visitLdcInsn(s1);
                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mtrace/wgc/demo/MTrace", "traceVarWrite", "(Ljava/lang/String;Ljava/lang/String;)V", false);
                                    break;
//                                case Opcodes.GETSTATIC:
//                                    mv.visitLdcInsn(s);
//                                    mv.visitLdcInsn(s1);
//                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mtrace/wgc/demo/MTrace", "traceStaticRead", "(Ljava/lang/String;Ljava/lang/String;)V", false);
//                                    break;
//                                case Opcodes.PUTSTATIC:
//                                    mv.visitLdcInsn(s);
//                                    mv.visitLdcInsn(s1);
//                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mtrace/wgc/demo/MTrace", "traceStaticWrite", "(Ljava/lang/String;Ljava/lang/String;)V", false);
//                                    break;
//                                case Opcodes.GETFIELD:
//                                    mv.visitInsn(Opcodes.DUP);
//                                    mv.visitLdcInsn(s1);
//                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mtrace/wgc/demo/MTrace", "traceFieldRead", "(Ljava/lang/Object;Ljava/lang/String;)V", false);
//                                    break;
//                                case Opcodes.PUTFIELD:
//                                    //o,v
//                                    mv.visitInsn(Opcodes.DUP2);
//                                    //o,v,o,v
//                                    mv.visitInsn(Opcodes.POP);
//                                    //o,v,o
//                                    mv.visitLdcInsn(s1);
//                                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "mtrace/wgc/demo/MTrace", "traceFieldWrite", "(Ljava/lang/Object;Ljava/lang/String;)V", false);
//                                    break;
                                default:break;
                            }

                            super.visitFieldInsn(i, s, s1, s2);
                        }
                    };
                }
            };
            classReader.accept(classVisitor, ClassReader.SKIP_DEBUG);
            return classWriter.toByteArray();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
