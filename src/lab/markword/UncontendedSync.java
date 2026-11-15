package lab.markword;

public class UncontendedSync {
    static final Object lock = new Object();
    static long count = 0;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        count = 0;
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            synchronized (lock) {
                count++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("count=" + count);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
