package lab.m6.registry;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class StaticList {
    static final int CALLS = 80;
    // Hardcoded IP list: node 2 crashed, no heartbeat eviction
    static final boolean[] nodeAlive = {true, true, false, true};
    static final AtomicInteger failedCalls = new AtomicInteger();

    static void call(int idx) {
        int target = idx % nodeAlive.length;
        if (!nodeAlive[target]) {
            failedCalls.incrementAndGet();
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] ws = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            ws[t] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int i = 0; i < CALLS; i++) call(i);
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            ws[t].start();
        }
        for (Thread w : ws) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("failedCalls=" + failedCalls.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
