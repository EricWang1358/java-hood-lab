package lab.m6.skiplist;

import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

public class LinearList {
    static final int N = 4000;
    static final int QUERIES = 40;
    static final ArrayList<Integer> list = new ArrayList<>(N);
    static final AtomicLong cmpCount = new AtomicLong();
    static volatile long sink;

    static {
        for (int i = 0; i < N; i++) list.add(i * 3);
    }

    static long rangeQuery(int lo, int hi) {
        long sum = 0;
        for (int i = 0; i < list.size(); i++) {
            cmpCount.incrementAndGet();
            int v = list.get(i);
            if (v >= lo && v <= hi) sum += v;
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
