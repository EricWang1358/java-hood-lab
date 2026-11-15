package lab.walredo;

public class DirectRandomFlush {
    public static void main(String[] args) {
        int ops = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        byte[] page = new byte[16384];
        int lostOrDirty = 0;
        long checksum = 0;
        for (int i = 0; i < ops; i++) {
            // Simulate 16KB random data-page flush on every single update
            int offset = (i * 131) % page.length;
            page[offset] = (byte) (i & 0x7F);
            for (int j = 0; j < page.length; j += 64) {
                checksum += page[j];
            }
            if (checksum < 0) lostOrDirty++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lostOrDirty=" + lostOrDirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
