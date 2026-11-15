package lab.markword;

import java.util.concurrent.CountDownLatch;

public class ContendedSync {
    static final Object lock = new Object();
    static long count = 0;

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        int threads = 4;
        int per = n / threads;
        count = 0;
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                try { ready.await(); } catch (InterruptedException ignored) {}
                for (int i = 0; i < per; i++) {
                    synchronized (lock) {
                        count++;
                    }
                }
                done.countDown();
            }).start();
        }
        long start = System.nanoTime();
        ready.countDown();
        done.await();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("count=" + count);
        System.out.println("expected=" + (per * threads));
        System.out.println("ms=" + ms);
    }
}
