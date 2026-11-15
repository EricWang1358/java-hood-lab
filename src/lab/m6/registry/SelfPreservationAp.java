package lab.m6.registry;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class SelfPreservationAp {
    static final int CALLS = 80;
    static final boolean[] nodeActuallyAlive = {true, true, true, false};
    static final boolean[] registryList = new boolean[4];
    static final AtomicInteger failedCalls = new AtomicInteger();

    static {
        // AP self-preservation: >15% heartbeats lost -> keep existing list rather than wiping all
        int missed = 3;
        boolean selfPreservation = missed > 1;
        for (int i = 0; i < 4; i++) {
            registryList[i] = selfPreservation ? true : nodeActuallyAlive[i];
        }
    }

    static void call(int idx) {
        int target = idx % 4;
        if (!nodeActuallyAlive[target]) {
            // Client retries next healthy node from preserved list
            int fallback = (target + 1) % 4;
            if (!nodeActuallyAlive[fallback]) failedCalls.incrementAndGet();
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
