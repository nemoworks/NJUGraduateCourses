package mtrace.wgc.demo;

import javassist.ClassClassPath;
import javassist.ClassPool;

import java.lang.instrument.Instrumentation;

public class App {
    public static void premain(String agentOps, Instrumentation inst) {
        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath(new ClassClassPath((MTrace.class)));
        inst.addTransformer(new ClassTransformer());
    }
}
