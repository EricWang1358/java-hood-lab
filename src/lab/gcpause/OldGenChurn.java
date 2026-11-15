package lab.gcpause;

public class OldGenChurn {
    static volatile int sink;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        byte[][] ring = new byte[512][];
        long start = System.nanoTime();
        int sum = 0;
        for (int i = 0; i < n; i++) {
            byte[] buf = new byte[32768];
            buf[0] = (byte) i;
            ring[i & 511] = buf;
            sum += buf[0];
        }
        sink = sum + ring[0][0];
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("processed=" + n);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
