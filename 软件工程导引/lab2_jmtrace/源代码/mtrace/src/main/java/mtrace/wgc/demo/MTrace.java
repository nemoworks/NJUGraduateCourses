package mtrace.wgc.demo;

public class MTrace {
    public static void printTid(){
        System.out.println("1111111111");
        System.out.println("Current Thread: "+Thread.currentThread());
    }

    public static void traceArrayRead(Object arr, int index) {
        String rw = "R";
        long treadNumber = Thread.currentThread().getId();
        long id = ((long)System.identityHashCode(arr) << 32) + index;
        String desc = arr.getClass().getCanonicalName();
        desc = desc.substring(0, desc.length() - 2) + "[" + index + "]";
        System.out.printf("%s\t%d\t%16x\t%s\n", rw, treadNumber, id, desc);
    }
    public static void traceArrayWrite(Object arr, int index) {
        String rw = "W";
        long t = Thread.currentThread().getId();
        long id = ((long)System.identityHashCode(arr) << 32) + index;
        String desc = arr.getClass().getCanonicalName();
        desc = desc.substring(0, desc.length() - 2) + "[" + index + "]";
        System.out.printf("%s\t%d\t%16x\t%s\n", rw, t, id, desc);
    }
    public static void traceVarRead(String owner, String name){
        String rw = "R";
        long t = Thread.currentThread().getId();
        long id = ((long)System.identityHashCode(owner) << 32) + name.hashCode();
        String desc = owner.replace('/','.')+"."+name;
        System.out.printf("%s\t%d\t%16x\t%s\n", rw, t, id, desc);
    }
    public static void traceVarWrite(String owner, String name){
        String rw = "W";
        long t = Thread.currentThread().getId();
        long id = ((long)System.identityHashCode(owner) << 32) + name.hashCode();
        String desc = owner.replace('/','.')+"."+name;
        System.out.printf("%s\t%d\t%16x\t%s\n", rw, t, id, desc);
    }
}
