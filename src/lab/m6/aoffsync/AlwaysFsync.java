package lab.m6.aoffsync;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class AlwaysFsync {
    static final int WRITES = 400;
    static final ReentrantLock diskLock = new ReentrantLock();
    static final AtomicInteger lostOnCrash = new AtomicInteger();
    static volatile long sink;

    static void appendAndFsync(int seq) {
        diskLock.lock();
        try {
            long x = seq;
            for (int i = 0; i < 4_000; i++) x = x * 31 + i; // synchronous fsync cost
            sink ^= x;
        } finally {
            diskLock.unlock();
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
                    for (int i = 0; i < WRITES; i++) appendAndFsync(i);
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
