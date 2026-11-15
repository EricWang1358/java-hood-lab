package lab.m6.breakdown;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class LogicalExpire {
    static final int WAVES = 15;
    static class Entry {
        final String val;
        final long expireAt;
        Entry(String val, long expireAt) { this.val = val; this.expireAt = expireAt; }
    }
    static final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<>();
    static final ReentrantLock lock = new ReentrantLock();
    static final ExecutorService pool = Executors.newSingleThreadExecutor();
    static final AtomicInteger dbRebuilds = new AtomicInteger();
    static volatile long clock = 0;
    static volatile long sink;

    static String rebuildFromDb(String key) {
        dbRebuilds.incrementAndGet();
        long x = key.hashCode();
        for (int i = 0; i < 120_000; i++) x = x * 31 + i;
        return key + ":" + (x & 0xffff);
    }

    static String getHot(String key) {
        Entry e = cache.get(key);
        if (e == null) {
            return key + ":init";
        }
        if (clock <= e.expireAt) {
            return e.val;
        }
        if (lock.tryLock()) {
            pool.submit(() -> {
                try {
                    Entry cur = cache.get(key);
                    if (cur != null && clock > cur.expireAt) {
                        cache.put(key, new Entry(rebuildFromDb(key), clock + 1));
                    }
                } finally {
                    lock.unlock();
                }
            });
        }
        return e.val;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        cache.put("hot-sku", new Entry("hot-sku:0", 0));
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> clock += 2);
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
        pool.shutdown();
        while (!pool.isTerminated()) Thread.yield();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("dbRebuilds=" + dbRebuilds.get());
        System.out.println("expected=" + WAVES);
        System.out.println("ms=" + ms);
    }
}
