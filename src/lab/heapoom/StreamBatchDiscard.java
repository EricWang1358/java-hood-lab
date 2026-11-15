package lab.heapoom;

public class StreamBatchDiscard {
    static volatile int sink;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int ok = 0;
        int sum = 0;
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            byte[] chunk = new byte[128 * 1024];
            chunk[0] = (byte) i;
            sum += chunk[0];
            ok++;
        }
        sink = sum;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
