package lab.longtrx;

public class LongTxHoldingView {
    public static void main(String[] args) {
        int updates = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // A long transaction holds an old ReadView -> Purge thread cannot reclaim ANY undo versions!
        int unpurgedUndo = updates;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("unpurgedUndo=" + unpurgedUndo);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
