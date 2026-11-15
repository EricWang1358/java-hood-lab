package lab.m6.registry;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class FastEvictStorm {
    static final int CALLS = 80;
    // Network jitter causes >85% heartbeats to miss strict 1s timeout -> registry evicts healthy nodes!
    static final boolean[] healthyNodes = {true, true, true, true};
    static final boolean[] evicted = new boolean[4];
    static final AtomicInteger failedCalls = new AtomicInteger();

    static {
        // Without self-preservation, transient network partition empties the registry table
        for (int i = 0; i < 4; i++) evicted[i] = true;
    }

    static void call(int idx) {
        int aliveCount = 0;
        for (boolean e : evicted) if (!e) aliveCount++;
        if (aliveCount == 0) {
            failedCalls.incrementAndGet(); // No available instance in registry!
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
