package lab.m6.aoffsync;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class NoFsync {
    static final int WRITES = 400;
    static final AtomicInteger bufferedSinceFsync = new AtomicInteger();
    static final AtomicInteger lostOnCrash = new AtomicInteger();
    static volatile long sink;

    static void appendAof(int seq) {
        int unfsynced = bufferedSinceFsync.incrementAndGet();
        if (seq % 100 == 99) lostOnCrash.accumulateAndGet(unfsynced, Math::max);
        sink ^= seq;
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
        System.out.println("lostOnCrash=" + lostOnCrash.get());
        System.out.println("expected=" + (threads * 40));
        System.out.println("ms=" + ms);
    }
}
