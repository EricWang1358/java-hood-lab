package lab.m6.rdbcow;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class StopTheWorldCopy {
    static final int PAGES = 256;
    static final int WRITES = 200;
    static final ReentrantLock stwLock = new ReentrantLock();
    static final int[] mem = new int[PAGES];
    static final AtomicInteger cowPages = new AtomicInteger();

    static void snapshot() {
        stwLock.lock();
        try {
            int[] copy = new int[PAGES];
            for (int i = 0; i < PAGES; i++) {
                copy[i] = mem[i];
                cowPages.incrementAndGet();
                for (int j = 0; j < 2_000; j++) copy[i] += 0;
            }
        } finally {
            stwLock.unlock();
        }
    }

    static void writeKey(int idx, int val) {
        stwLock.lock();
        try { mem[idx % PAGES] = val; } finally { stwLock.unlock(); }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads + 1);
        Thread bg = new Thread(() -> {
            try { barrier.await(); snapshot(); } catch (Exception e) { throw new RuntimeException(e); }
        });
        bg.start();
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            workers[t] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int i = 0; i < WRITES; i++) writeKey(tid * 13 + i, i);
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
