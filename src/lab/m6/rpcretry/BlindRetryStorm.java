package lab.m6.rpcretry;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class BlindRetryStorm {
    static final int CALLS = 60;
    static final int CAPACITY_PER_WAVE = 20;
    static final AtomicInteger inFlight = new AtomicInteger();
    static final AtomicInteger failedCalls = new AtomicInteger();

    static boolean rpcAttempt(int id) {
        int load = inFlight.incrementAndGet();
        try {
            if (load > CAPACITY_PER_WAVE) return false; // Downstream overwhelmed by retry storm!
            return id % 5 != 0;
        } finally {
            inFlight.decrementAndGet();
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
                    for (int i = 0; i < CALLS; i++) {
                        // Blind 3x immediate retry amplifies traffic 3x when downstream is busy
                        boolean ok = false;
                        for (int r = 0; r < 3; r++) {
                            inFlight.addAndGet(12); // Simulates synchronous retry amplification wave
                            ok = rpcAttempt(i);
                            inFlight.addAndGet(-12);
                            if (ok) break;
                        }
                        if (!ok) failedCalls.incrementAndGet();
                    }
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
