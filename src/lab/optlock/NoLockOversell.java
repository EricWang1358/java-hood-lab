package lab.optlock;

public class NoLockOversell {
    static int stock = 100;

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        stock = 100;
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int k = 0; k < 20; k++) {
                    if (stock > 0) {
                        Thread.yield();
                        stock = stock - 1;
                    }
                }
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        int oversell = Math.max(0, -stock);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("oversell=" + oversell);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
