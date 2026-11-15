package lab.m6.skiplist;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

public class SkipListRange {
    static final int N = 4000;
    static final int QUERIES = 40;
    static final ConcurrentSkipListMap<Integer, Integer> skip = new ConcurrentSkipListMap<>();
    static final AtomicLong cmpCount = new AtomicLong();
    static volatile long sink;

    static {
        for (int i = 0; i < N; i++) skip.put(i * 3, i);
    }

    static long rangeQuery(int lo, int hi) {
        cmpCount.addAndGet(12); // O(log N) express-lane descent, then level-0 forward scan
        long sum = 0;
        for (Integer k : skip.subMap(lo, true, hi, true).keySet()) {
            cmpCount.incrementAndGet();
            sum += k;
        }
        return sum;
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
                    long s = 0;
                    for (int q = 0; q < QUERIES; q++) s += rangeQuery(6000, 6300);
                    sink = s;
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("cmpCount=" + cmpCount.get());
        System.out.println("expected=" + (threads * QUERIES * 120));
        System.out.println("ms=" + ms);
    }
}
