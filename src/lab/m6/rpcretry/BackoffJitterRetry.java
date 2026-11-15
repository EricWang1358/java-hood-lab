package lab.m6.rpcretry;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class BackoffJitterRetry {
    static final int CALLS = 60;
    static final AtomicInteger retryBudget = new AtomicInteger(100);
    static final AtomicInteger failedCalls = new AtomicInteger();

    static boolean rpcAttempt(int id, int attempt) {
        return (id % 5 != 0) || attempt > 0;
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
                        boolean ok = rpcAttempt(i, 0);
                        int attempt = 1;
                        while (!ok && attempt <= 2 && retryBudget.get() > 0) {
                            retryBudget.decrementAndGet();
                            Thread.yield(); // Exponential backoff + jitter spreads retry waves
                            ok = rpcAttempt(i, attempt++);
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
