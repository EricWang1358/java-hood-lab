package lab.m6.avalanche;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitFallback {
    static final int TICKS = 40;
    static final int KEYS = 60;
    static final int BASE_TTL = 10;
    static final ConcurrentHashMap<Integer, Long> expireAt = new ConcurrentHashMap<>();
    static final AtomicInteger tickBudget = new AtomicInteger(12);
    static final AtomicInteger peakDbQps = new AtomicInteger();
    static final AtomicInteger tickDbHits = new AtomicInteger();
    static volatile long sink;

    static {
        for (int k = 0; k < KEYS; k++) expireAt.put(k, (long) BASE_TTL);
    }

    static String loadDb(int key) {
        int cur = tickDbHits.incrementAndGet();
        peakDbQps.accumulateAndGet(cur, Math::max);
        long x = key * 31L;
        for (int i = 0; i < 15_000; i++) x = x * 31 + i;
        return "v-" + key + ":" + (x & 0xff);
    }

    static String get(int key, int tick) {
        Long exp = expireAt.get(key);
        if (exp == null || tick >= exp) {
            if (tickBudget.getAndDecrement() > 0) {
                String val = loadDb(key);
                expireAt.put(key, (long) (tick + BASE_TTL));
                return val;
            }
            return "fallback-" + key;
        }
        return "cached-" + key;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> {
            tickDbHits.set(0);
            tickBudget.set(12);
        });
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            int tid = t;
            workers[t] = new Thread(() -> {
                try {
                    long sum = 0;
                    for (int tick = 0; tick < TICKS; tick++) {
                        barrier.await();
                        for (int k = tid; k < KEYS; k += threads) {
                            sum += get(k, tick).hashCode();
                        }
                    }
                    sink = sum;
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("peakDbQps=" + peakDbQps.get());
        System.out.println("expected=15");
        System.out.println("ms=" + ms);
    }
}
