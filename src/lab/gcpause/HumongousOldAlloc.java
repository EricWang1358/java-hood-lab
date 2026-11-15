package lab.gcpause;

public class HumongousOldAlloc {
    static volatile int sink;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int sum = 0;
        for (int i = 0; i < n; i++) {
            if ((i & 3) == 0) {
                byte[] big = new byte[1048576];
                big[0] = (byte) i;
                sum += big[0];
            }
        }
        sink = sum;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("processed=" + n);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
