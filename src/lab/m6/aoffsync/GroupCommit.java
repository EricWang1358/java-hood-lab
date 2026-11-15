package lab.m6.aoffsync;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class GroupCommit {
    static final int WRITES = 400;
    static final ReentrantLock diskLock = new ReentrantLock();
    static final AtomicInteger pending = new AtomicInteger();
    static final AtomicInteger lostOnCrash = new AtomicInteger();
    static volatile long sink;

    static void appendAndGroupFsync(int seq) {
        int cur = pending.incrementAndGet();
        if (cur % 16 == 0) {
            diskLock.lock();
            try {
                lostOnCrash.accumulateAndGet(Math.min(16, pending.get()), Math::max);
                long x = seq;
                for (int i = 0; i < 4_000; i++) x = x * 31 + i;
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
                    for (int i = 0; i < WRITES; i++) appendAndGroupFsync(i);
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lostOnCrash=" + lostOnCrash.get());
        System.out.println("expected=" + (threads * 40));
        System.out.println("ms=" + ms);
    }
}
