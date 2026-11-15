package lab.cacheaside;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleFlight {
    static final int WAVES = 20;
    static final int KEYS = 30;
    static final int HOT = 6;
    static ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>(256);
    static final AtomicInteger dbLoads = new AtomicInteger();
    static volatile long sink;

    static String loadDb(String key) {
        dbLoads.incrementAndGet();
        long x = key.hashCode();
        for (int i = 0; i < 260_000; i++) x = x * 31 + i;
        return key + ":" + (x & 0xffff);
    }

    static String getValue(String key) {
        return cache.computeIfAbsent(key, k -> loadDb(k));
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        String[] keys = new String[KEYS];
        for (int i = 0; i < KEYS; i++) keys[i] = "k-" + i;
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> cache = new ConcurrentHashMap<>(256));
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            int shift = t * ((KEYS - HOT) / threads);
            workers[t] = new Thread(() -> {
                try {
                    long sum = 0;
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        for (int i = 0; i < KEYS; i++) {
                            int k = (i < HOT) ? i : HOT + (i - HOT + shift) % (KEYS - HOT);
                            sum += getValue(keys[k]).hashCode();
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
        System.out.println("dbLoads=" + dbLoads.get());
        System.out.println("expected=" + (WAVES * KEYS));
        System.out.println("ms=" + ms);
    }
}
