package lab.adder;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

public class Sync {
    static final int WAVES = 1_000;
    static final long LIMIT = 2_500;
    static long issued = 0;
    static final AtomicLong sold = new AtomicLong();

    static synchronized boolean grab() {
        if (issued < LIMIT) {
            issued++;
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> issued = 0);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try {
                    long mine = 0;
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        while (grab()) mine++;
                    }
                    sold.addAndGet(mine);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("sold=" + sold.get());
        System.out.println("expected=" + (WAVES * LIMIT));
        System.out.println("ms=" + ms);
    }
}
