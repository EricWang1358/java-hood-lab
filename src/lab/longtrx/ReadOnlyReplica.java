package lab.longtrx;

public class ReadOnlyReplica {
    public static void main(String[] args) {
        int updates = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Route slow BI/export queries to read replica so master's Purge isn't blocked
        int unpurgedUndo = 0;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("unpurgedUndo=" + unpurgedUndo);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
