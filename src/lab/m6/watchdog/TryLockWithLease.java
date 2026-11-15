package lab.m6.watchdog;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TryLockWithLease {
    static final int ROUNDS = 20;
    static final ReentrantLock lock = new ReentrantLock();
    static final AtomicInteger inside = new AtomicInteger();
    static final AtomicInteger doubleEntries = new AtomicInteger();

    static void workWithBoundedWait() throws InterruptedException {
        if (lock.tryLock(5, TimeUnit.MILLISECONDS)) {
            try {
                int cur = inside.incrementAndGet();
                if (cur > 1) doubleEntries.incrementAndGet();
                long x = 0;
                for (int i = 0; i < 10_000; i++) x = x * 31 + i;
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
                ws[t] = new Thread(() -> {
                    try { barrier.await(); workWithBoundedWait(); } catch (Exception e) { throw new RuntimeException(e); }
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
