package lab.walredo;

public class RedoPlusUndo {
    public static void main(String[] args) {
        int ops = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int lostOrDirty = 0;
        byte[] redoBuf = new byte[4096];
        for (int i = 0; i < ops; i++) {
            int oldVal = 100;
            int undoOldImage = oldVal;
            int liveVal = 200;
            redoBuf[i & 4095] = (byte) (liveVal & 0x7F);
            boolean rollback = (i % 5 == 0);
            if (rollback) {
                liveVal = undoOldImage; // Undo restores old image cleanly
            }
            if (rollback && liveVal != oldVal) lostOrDirty++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lostOrDirty=" + lostOrDirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
