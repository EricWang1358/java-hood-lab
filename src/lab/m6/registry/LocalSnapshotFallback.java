package lab.m6.registry;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalSnapshotFallback {
    static final int CALLS = 80;
    static final boolean registryDown = true;
    static final boolean[] localDiskSnapshot = {true, true, true, false};
    static final AtomicInteger failedCalls = new AtomicInteger();

    static void call(int idx) {
        // Even if the registry server itself is down, client falls back to local snapshot + health check
        boolean[] list = registryDown ? localDiskSnapshot : localDiskSnapshot;
        int target = idx % list.length;
        if (!list[target]) {
            // Pick next healthy instance in local snapshot
            target = (target + 1) % list.length;
        }
        if (!list[target]) failedCalls.incrementAndGet();
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
