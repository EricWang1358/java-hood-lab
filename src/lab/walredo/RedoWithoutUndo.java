package lab.walredo;

public class RedoWithoutUndo {
    public static void main(String[] args) {
        int ops = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int lostOrDirty = 0;
        for (int i = 0; i < ops; i++) {
            int committedVal = 100;
            int liveVal = 200; // Modified in memory + Redo written
            boolean rollback = (i % 5 == 0);
            if (rollback) {
                // Without Undo Log, we have no old-image to restore -> dirty value remains!
                if (liveVal != committedVal) lostOrDirty++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lostOrDirty=" + lostOrDirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
