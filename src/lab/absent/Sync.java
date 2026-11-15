package lab.absent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Sync {
    static final int WAVES = 200;
    static final int KEYS = 64;
    static Map<String, String> cache = new HashMap<>(256);
    static final AtomicInteger inits = new AtomicInteger();

    static String createConn(String key) {
        inits.incrementAndGet();
        long x = key.hashCode();
        for (int i = 0; i < 600; i++) x = x * 31 + i;
        return key + ":" + (x & 0xff);
    }

    static synchronized String getConn(String key) {
        if (!cache.containsKey(key)) {
            cache.put(key, createConn(key));
        }
        return cache.get(key);
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        String[] keys = new String[KEYS];
        for (int i = 0; i < KEYS; i++) keys[i] = "db-" + i;
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> cache = new HashMap<>(256));
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try {
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        for (String k : keys) getConn(k);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("inits=" + inits.get());
        System.out.println("expected=" + (WAVES * KEYS));
        System.out.println("ms=" + ms);
    }
}
