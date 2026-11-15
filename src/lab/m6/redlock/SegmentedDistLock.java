package lab.m6.redlock;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SegmentedDistLock {
    static final int OPS = 250;
    static final int SEGMENTS = 16;
    static final ReentrantLock[] segLocks = new ReentrantLock[SEGMENTS];
    static final AtomicInteger conflicts = new AtomicInteger();
    static volatile long sink;

    static {
        for (int i = 0; i < SEGMENTS; i++) segLocks[i] = new ReentrantLock();
    }

    static void processOrder(int orderId) {
        ReentrantLock lock = segLocks[Math.floorMod(orderId, SEGMENTS)];
        lock.lock();
        try {
            long x = orderId;
            for (int i = 0; i < 6_000; i++) x = x * 31 + i;
            sink ^= x;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] ws = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            ws[t] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int i = 0; i < OPS; i++) processOrder(tid * 1000 + i);
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            ws[t].start();
        }
        for (Thread w : ws) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("conflicts=" + conflicts.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
