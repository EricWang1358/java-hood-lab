package lab.m6.hotkey;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SingleShard {
    static final int OPS = 8_000;
    static final ReentrantLock shardLock = new ReentrantLock();
    static final AtomicInteger shardHits = new AtomicInteger();
    static volatile String hotValue = "sku-hot:99";
    static volatile long sink;

    static String readHot(String key) {
        shardLock.lock();
        try {
            shardHits.incrementAndGet();
            long x = key.hashCode();
            for (int i = 0; i < 400; i++) x = x * 31 + i;
            return hotValue + ":" + (x & 0xf);
        } finally {
            shardLock.unlock();
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
                    long sum = 0;
                    for (int i = 0; i < OPS; i++) sum += readHot("hot-1").hashCode();
                    sink = sum;
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("shardHits=" + shardHits.get());
        System.out.println("expected=" + (threads * 500));
        System.out.println("ms=" + ms);
    }
}
