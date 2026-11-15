package lab.m6.bigkey;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class PipelineBatch {
    static final int BATCHES = 25;
    static final int ITEMS = 40;
    static final ReentrantLock redisSingleThread = new ReentrantLock();
    static final AtomicInteger rttCount = new AtomicInteger();
    static volatile long sink;

    static void fetchPipeline() {
        rttCount.incrementAndGet();
        redisSingleThread.lock();
        try {
            long x = 0;
            for (int i = 0; i < ITEMS * 600; i++) x = x * 31 + i;
            sink ^= x;
        } finally {
            redisSingleThread.unlock();
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
                    for (int b = 0; b < BATCHES; b++) fetchPipeline();
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
