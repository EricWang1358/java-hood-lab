package lab.semgate;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class SemGate {
    static final int LIMIT = 2;
    static final int WAVES = 12;
    static final Semaphore sem = new Semaphore(LIMIT);
    static final AtomicInteger inFlight = new AtomicInteger();
    static final AtomicInteger peak = new AtomicInteger();
    static final AtomicInteger okCalls = new AtomicInteger();

    static void callDownstream() {
        int cur = inFlight.incrementAndGet();
        peak.accumulateAndGet(cur, Math::max);
        LockSupport.parkNanos(1_850_000L);
        if (cur <= LIMIT && inFlight.get() <= LIMIT) {
            okCalls.incrementAndGet();
        }
        inFlight.decrementAndGet();
    }

    static void enterGate() throws InterruptedException {
        sem.acquire();
        try {
            callDownstream();
        } finally {
            sem.release();
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try {
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        enterGate();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("okCalls=" + okCalls.get());
        System.out.println("expected=" + (threads * WAVES));
        System.out.println("peak=" + peak.get());
        System.out.println("ms=" + ms);
    }
}
