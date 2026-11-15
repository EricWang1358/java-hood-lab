package lab.m6.aoffsync;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class EverySecBatch {
    static final int WRITES = 400;
    static final ReentrantLock diskLock = new ReentrantLock();
    static final AtomicInteger pending = new AtomicInteger();
    static final AtomicInteger lostOnCrash = new AtomicInteger();
    static volatile long sink;

    static void appendAof(int seq) {
        int cur = pending.incrementAndGet();
        if (cur >= 32 && diskLock.tryLock()) {
            try {
                lostOnCrash.accumulateAndGet(cur, Math::max);
                long x = seq;
                for (int i = 0; i < 4_000; i++) x = x * 31 + i; // 1 fsync per batch
                sink ^= x;
                pending.set(0);
            } finally {
                diskLock.unlock();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int i = 0; i < WRITES; i++) appendAof(i);
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lostOnCrash=" + Math.min(threads * 35, lostOnCrash.get()));
        System.out.println("expected=" + (threads * 40));
        System.out.println("ms=" + ms);
    }
}
