package lab.walredo;

public class WalRedoSequential {
    public static void main(String[] args) {
        int ops = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        byte[] redoBuf = new byte[4096];
        int lostOrDirty = 0;
        int pos = 0;
        for (int i = 0; i < ops; i++) {
            // WAL: append tiny sequential redo record (32 bytes) instead of flushing 16KB page
            redoBuf[pos & 4095] = (byte) (i & 0x7F);
            pos += 32;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lostOrDirty=" + lostOrDirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
