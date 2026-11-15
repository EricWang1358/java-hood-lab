package lab.m6.breaker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeoutFailFast {
    static final int SHARED_POOL = 8;
    static final int REQ_PER_THREAD = 20;
    static final Semaphore sharedThreads = new Semaphore(SHARED_POOL);
    static final AtomicInteger healthyDropped = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                for (int i = 0; i < REQ_PER_THREAD; i++) {
                    // Slow call A fails fast on short timeout and immediately releases slot
                    sharedThreads.acquireUninterruptibly();
                    sharedThreads.release();
                    // Healthy call B always finds available threads
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
