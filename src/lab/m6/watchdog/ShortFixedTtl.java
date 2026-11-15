package lab.m6.watchdog;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ShortFixedTtl {
    static final int ROUNDS = 20;
    static final AtomicLong lockExpireTick = new AtomicLong(-1);
    static final AtomicInteger inside = new AtomicInteger();
    static final AtomicInteger doubleEntries = new AtomicInteger();
    static volatile int tick = 0;

    static void work(int tid) {
        if (tid == 0) {
            lockExpireTick.set(tick + 2); // short TTL = 2 ticks
            inside.incrementAndGet();
            for (int i = 0; i < 6; i++) { tick++; Thread.yield(); }
            inside.decrementAndGet();
        } else {
            Thread.yield();
            if (tick > lockExpireTick.get()) {
                lockExpireTick.set(tick + 2);
                int cur = inside.incrementAndGet();
                if (cur > 1) doubleEntries.incrementAndGet();
                inside.decrementAndGet();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            tick += 10;
            Thread[] ws = new Thread[threads];
            for (int t = 0; t < threads; t++) {
                final int tid = t;
                ws[t] = new Thread(() -> {
                    try {
                        barrier.await();
                        work(tid);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
                ws[t].start();
            }
            for (Thread w : ws) w.join();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("doubleEntries=" + doubleEntries.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
