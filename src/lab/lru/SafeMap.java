package lab.lru;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SafeMap {
    static final int WAVES = 200;
    static final int READS = 600;
    static final String[] KEYS = new String[16];
    static Map<String, String> cache;
    static final AtomicBoolean broken = new AtomicBoolean();
    static final AtomicInteger okWaves = new AtomicInteger();

    static {
        for (int i = 0; i < KEYS.length; i++) KEYS[i] = "k" + i;
    }

    static String getItem(String key) {
        return cache.get(key);
    }

    static Map<String, String> newCache() {
        Map<String, String> m = new ConcurrentHashMap<>(64);
        for (String k : KEYS) m.put(k, k);
        return m;
    }

    static boolean isIntact() {
        if (broken.get()) return false;
        try {
            Iterator<String> it = cache.values().iterator();
            int c = 0;
            while (it.hasNext() && c <= 20) {
                it.next();
                c++;
            }
            return c == KEYS.length;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        cache = newCache();
        AtomicInteger wave = new AtomicInteger();
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> {
            if (wave.getAndIncrement() > 0) {
                if (isIntact()) okWaves.incrementAndGet();
                broken.set(false);
                cache = newCache();
            }
        });
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            int offset = t * 3;
            workers[t] = new Thread(() -> {
                try {
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        try {
                            for (int i = 0; i < READS; i++) {
                                getItem(KEYS[(i + offset) & 15]);
                            }
                        } catch (Throwable ex) {
                            broken.set(true);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        if (isIntact()) okWaves.incrementAndGet();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("okWaves=" + okWaves.get());
        System.out.println("expected=" + WAVES);
        System.out.println("ms=" + ms);
    }
}
