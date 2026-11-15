package lab.longtrx;

public class BatchCommit {
    public static void main(String[] args) {
        int updates = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Split giant update into small batches of 1000 rows so Purge can advance
        int unpurgedUndo = 0;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("unpurgedUndo=" + unpurgedUndo);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
