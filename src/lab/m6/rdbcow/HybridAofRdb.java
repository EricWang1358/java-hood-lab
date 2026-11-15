package lab.m6.rdbcow;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HybridAofRdb {
    static final int PAGES = 256;
    static final int WRITES = 200;
    static final int[] mem = new int[PAGES];
    static final AtomicBoolean[] dirtyCow = new AtomicBoolean[PAGES];
    static final AtomicInteger cowPages = new AtomicInteger();

    static {
        for (int i = 0; i < PAGES; i++) dirtyCow[i] = new AtomicBoolean(false);
    }

    static void bgsaveHybrid() {
        long x = 0;
        for (int i = 0; i < PAGES * 500; i++) x = x * 31 + i; // faster hybrid RDB+AOF tail
        if (x == 42) System.out.print("");
    }

    static void writeKey(int idx, int val) {
        int page = (idx % 32); // localized hot set during short fork window
        if (dirtyCow[page].compareAndSet(false, true)) cowPages.incrementAndGet();
        mem[page] = val;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads + 1);
        Thread bg = new Thread(() -> {
            try { barrier.await(); bgsaveHybrid(); } catch (Exception e) { throw new RuntimeException(e); }
        });
        bg.start();
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            workers[t] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int i = 0; i < WRITES; i++) writeKey(tid + i, i);
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            workers[t].start();
        }
        bg.join();
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("cowPages=" + cowPages.get());
        System.out.println("expected=64");
        System.out.println("ms=" + ms);
    }
}
