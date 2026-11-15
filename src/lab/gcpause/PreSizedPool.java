package lab.gcpause;

public class PreSizedPool {
    static volatile int sink;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        byte[] reusable = new byte[32768];
        long start = System.nanoTime();
        int sum = 0;
        for (int i = 0; i < n; i++) {
            reusable[0] = (byte) i;
            sum += reusable[0];
        }
        sink = sum;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("processed=" + n);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
