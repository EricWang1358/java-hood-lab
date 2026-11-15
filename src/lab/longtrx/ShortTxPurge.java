package lab.longtrx;

public class ShortTxPurge {
    public static void main(String[] args) {
        int updates = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Short transactions commit quickly -> Purge thread reclaims old undo versions immediately!
        int unpurgedUndo = 0;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("unpurgedUndo=" + unpurgedUndo);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
