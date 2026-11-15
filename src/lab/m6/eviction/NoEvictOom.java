package lab.m6.eviction;

import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class NoEvictOom {
    static final int CAP = 64;
    static final int OPS = 800;
    static final AtomicInteger misses = new AtomicInteger();

    static int runWorkload(int seed) {
        HashMap<Integer, Integer> map = new HashMap<>(CAP);
        int miss = 0;
        for (int i = 0; i < OPS; i++) {
            int key = (i % 10 < 8) ? (i % 16) : (100 + (i + seed) % 200);
            if (!map.containsKey(key)) {
                miss++;
                if (map.size() < CAP) map.put(key, i);
                // noeviction: rejects new writes once full -> hot keys never enter if cold keys filled it!
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
                try {
                    barrier.await();
                    // fill with cold keys first
                    HashMap<Integer, Integer> pre = new HashMap<>(CAP);
                    for (int k = 100; k < 100 + CAP; k++) pre.put(k, k);
                    int m = 0;
                    for (int i = 0; i < OPS; i++) {
                        int key = (i % 10 < 8) ? (i % 16) : (100 + (i + tid) % 200);
                        if (!pre.containsKey(key)) m++;
                    }
                    misses.addAndGet(m);
                } catch (Exception e) { throw new RuntimeException(e); }
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
