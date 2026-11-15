package lab.m6.bigkey;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SplitSmallKeys {
    static final int BATCHES = 25;
    static final int SHARDS = 8;
    static final ReentrantLock[] shardLoops = new ReentrantLock[SHARDS];
    static final AtomicInteger rttCount = new AtomicInteger();
    static volatile long sink;

    static {
        for (int i = 0; i < SHARDS; i++) shardLoops[i] = new ReentrantLock();
    }

    static void fetchSmallKey(int id) {
        rttCount.incrementAndGet();
        ReentrantLock lock = shardLoops[id % SHARDS];
        lock.lock();
        try {
            long x = id;
            for (int i = 0; i < 4_000; i++) x = x * 31 + i;
            sink ^= x;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            workers[t] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int b = 0; b < BATCHES; b++) fetchSmallKey(tid + b);
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("rttCount=" + rttCount.get());
        System.out.println("expected=" + (threads * BATCHES));
        System.out.println("ms=" + ms);
    }
}
