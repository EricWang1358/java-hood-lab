package lab.m6.penetration;

import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class BloomFilter {
    static final int WAVES = 10;
    static final int VALID_KEYS = 20;
    static final int INVALID_KEYS = 80;
    static final int BIT_SIZE = 2048;
    static final BitSet bloom = new BitSet(BIT_SIZE);
    static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>(128);
    static final AtomicInteger dbHits = new AtomicInteger();
    static volatile long sink;

    static {
        for (int i = 0; i < VALID_KEYS; i++) {
            String k = "p-" + i;
            bloom.set(hash1(k));
            bloom.set(hash2(k));
            bloom.set(hash3(k));
        }
    }

    static int hash1(String s) { return Math.floorMod(s.hashCode(), BIT_SIZE); }
    static int hash2(String s) { return Math.floorMod(s.hashCode() * 31 + 17, BIT_SIZE); }
    static int hash3(String s) { return Math.floorMod(s.hashCode() * 131 + 7, BIT_SIZE); }

    static boolean mightContain(String key) {
        return bloom.get(hash1(key)) && bloom.get(hash2(key)) && bloom.get(hash3(key));
    }

    static String queryDb(String key) {
        dbHits.incrementAndGet();
        long x = key.hashCode();
        for (int i = 0; i < 60_000; i++) x = x * 31 + i;
        int id = Integer.parseInt(key.substring(2));
        return id < VALID_KEYS ? "val-" + id + ":" + (x & 0xff) : null;
    }

    static String getProduct(String key) {
        if (!mightContain(key)) return null;
        return cache.computeIfAbsent(key, k -> queryDb(k));
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        String[] reqs = new String[VALID_KEYS + INVALID_KEYS];
        for (int i = 0; i < reqs.length; i++) reqs[i] = "p-" + i;
        CyclicBarrier barrier = new CyclicBarrier(threads, cache::clear);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            int offset = t * 17;
            workers[t] = new Thread(() -> {
                try {
                    long sum = 0;
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        for (int i = 0; i < reqs.length; i++) {
                            String k = reqs[(i + offset) % reqs.length];
                            String v = getProduct(k);
                            if (v != null) sum += v.hashCode();
                        }
                    }
                    sink = sum;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("dbHits=" + dbHits.get());
        System.out.println("expected=" + (WAVES * VALID_KEYS));
        System.out.println("ms=" + ms);
    }
}
