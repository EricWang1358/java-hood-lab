package lab.m6.eviction;

import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class LfuFrequency {
    static final int CAP = 64;
    static final int OPS = 800;
    static final AtomicInteger misses = new AtomicInteger();

    static int runWorkload(int seed) {
        HashMap<Integer, Integer> freq = new HashMap<>(CAP);
        int miss = 0;
        for (int i = 0; i < OPS; i++) {
            int key = (i % 10 < 8) ? (i % 16) : (100 + (i + seed) % 200);
            Integer f = freq.get(key);
            if (f != null) {
                freq.put(key, Math.min(255, f + 1));
            } else {
                miss++;
                if (freq.size() >= CAP) {
                    int minKey = -1, minF = Integer.MAX_VALUE;
                    for (var e : freq.entrySet()) {
                        if (e.getValue() < minF) { minF = e.getValue(); minKey = e.getKey(); }
                    }
                    freq.remove(minKey);
                }
                freq.put(key, 5); // Morris counter initial frequency
            }
        }
        return miss;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            workers[t] = new Thread(() -> {
                try { barrier.await(); misses.addAndGet(runWorkload(tid)); } catch (Exception e) { throw new RuntimeException(e); }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("misses=" + misses.get());
        System.out.println("expected=" + (threads * 200));
        System.out.println("ms=" + ms);
    }
}
