package lab.m6.breaker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreakerSm {
    static final int SHARED_POOL = 8;
    static final int REQ_PER_THREAD = 20;
    static final int CLOSED = 0, OPEN = 1, HALF_OPEN = 2;
    static final AtomicInteger state = new AtomicInteger(CLOSED);
    static final AtomicInteger failStreak = new AtomicInteger();
    static final Semaphore sharedThreads = new Semaphore(SHARED_POOL);
    static final AtomicInteger healthyDropped = new AtomicInteger();

    static boolean callServiceA(int seq) {
        if (state.get() == OPEN) {
            if (seq % 15 == 0 && state.compareAndSet(OPEN, HALF_OPEN)) {
                // Probe recovery
                state.set(CLOSED);
                failStreak.set(0);
                return true;
            }
            return false; // Short-circuit fallback without touching thread pool!
        }
        if (seq < 5) {
            if (failStreak.incrementAndGet() >= 3) state.set(OPEN);
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                for (int i = 0; i < REQ_PER_THREAD; i++) {
                    callServiceA(i);
                    if (!sharedThreads.tryAcquire()) {
                        healthyDropped.incrementAndGet();
                    } else {
                        sharedThreads.release();
                    }
                }
                done.countDown();
            }).start();
        }
        done.await();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("healthyDropped=" + healthyDropped.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
