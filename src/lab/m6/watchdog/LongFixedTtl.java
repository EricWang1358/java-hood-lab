package lab.m6.watchdog;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LongFixedTtl {
    static final int ROUNDS = 20;
    static final AtomicBoolean locked = new AtomicBoolean(false);
    static final AtomicInteger inside = new AtomicInteger();
    static final AtomicInteger doubleEntries = new AtomicInteger();

    static void work(boolean crash) {
        if (locked.compareAndSet(false, true)) {
            int cur = inside.incrementAndGet();
            if (cur > 1) doubleEntries.incrementAndGet();
            long x = 0;
            for (int i = 0; i < 40_000; i++) x = x * 31 + i;
            if (x == 42) System.out.print("");
            inside.decrementAndGet();
            if (!crash) locked.set(false);
            // if crashed with long TTL (e.g. 5 min), lock stays stuck for 5 min!
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            locked.set(false);
            final int round = r;
            Thread[] ws = new Thread[threads];
            for (int t = 0; t < threads; t++) {
                final int tid = t;
                ws[t] = new Thread(() -> {
                    try {
                        barrier.await();
                        if (round % 5 == 0 && tid != 0) Thread.yield();
                        work(round % 5 == 0 && tid == 0);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
                ws[t].start();
            }
            for (Thread w : ws) w.join();
            if (locked.get()) doubleEntries.incrementAndGet(); // stuck waiting for long TTL
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("doubleEntries=" + doubleEntries.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
