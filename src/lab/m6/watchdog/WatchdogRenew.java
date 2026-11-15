package lab.m6.watchdog;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class WatchdogRenew {
    static final int ROUNDS = 20;
    static final ReentrantLock lock = new ReentrantLock();
    static final AtomicInteger inside = new AtomicInteger();
    static final AtomicInteger doubleEntries = new AtomicInteger();

    static void workWithWatchdog(boolean slow) {
        if (lock.tryLock()) {
            try {
                int cur = inside.incrementAndGet();
                if (cur > 1) doubleEntries.incrementAndGet();
                int steps = slow ? 60_000 : 10_000;
                long x = 0;
                for (int i = 0; i < steps; i++) {
                    x = x * 31 + i;
                    // every 10s (1/3 of 30s lease), watchdog renews TTL while holder is alive
                }
                if (x == 42) System.out.print("");
                inside.decrementAndGet();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            Thread[] ws = new Thread[threads];
            for (int t = 0; t < threads; t++) {
                final int tid = t;
                ws[t] = new Thread(() -> {
                    try { barrier.await(); workWithWatchdog(tid == 0); } catch (Exception e) { throw new RuntimeException(e); }
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
