package lab.m6.breakdown;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MutexTrySpin {
    static final int WAVES = 15;
    static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    static final ReentrantLock lock = new ReentrantLock();
    static final AtomicInteger dbRebuilds = new AtomicInteger();
    static volatile long sink;

    static String rebuildFromDb(String key) {
        dbRebuilds.incrementAndGet();
        long x = key.hashCode();
        for (int i = 0; i < 120_000; i++) x = x * 31 + i;
        return key + ":" + (x & 0xffff);
    }

    static String getHot(String key) {
        while (true) {
            String val = cache.get(key);
            if (val != null) return val;
            if (lock.tryLock()) {
                try {
                    val = cache.get(key);
                    if (val == null) {
                        val = rebuildFromDb(key);
                        cache.put(key, val);
                    }
                    return val;
                } finally {
                    lock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads, cache::clear);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try {
                    long sum = 0;
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        for (int r = 0; r < 20; r++) sum += getHot("hot-sku").hashCode();
                    }
                    sink = sum;
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("dbRebuilds=" + dbRebuilds.get());
        System.out.println("expected=" + WAVES);
        System.out.println("ms=" + ms);
    }
}
