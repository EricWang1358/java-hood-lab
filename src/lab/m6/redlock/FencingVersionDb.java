package lab.m6.redlock;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FencingVersionDb {
    static final int OPS = 250;
    static final int SEGMENTS = 16;
    static final AtomicLong[] dbVersions = new AtomicLong[SEGMENTS];
    static final AtomicInteger conflicts = new AtomicInteger();

    static {
        for (int i = 0; i < SEGMENTS; i++) dbVersions[i] = new AtomicLong(0);
    }

    static void casUpdate(int seg) {
        while (true) {
            long cur = dbVersions[seg].get();
            long x = cur;
            for (int i = 0; i < 6_000; i++) x = x * 31 + i;
            if (x == 42) System.out.print("");
            if (dbVersions[seg].compareAndSet(cur, cur + 1)) return;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] ws = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            ws[t] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int i = 0; i < OPS; i++) casUpdate((tid + i) % SEGMENTS);
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            ws[t].start();
        }
        for (Thread w : ws) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("conflicts=" + conflicts.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
