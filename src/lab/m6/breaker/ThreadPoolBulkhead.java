package lab.m6.breaker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolBulkhead {
    static final int POOL_A = 4;
    static final int POOL_B = 8;
    static final int REQ_PER_THREAD = 20;
    static final Semaphore bulkheadA = new Semaphore(POOL_A);
    static final Semaphore bulkheadB = new Semaphore(POOL_B);
    static final AtomicInteger healthyDropped = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CountDownLatch slowStarted = new CountDownLatch(POOL_A);
        CountDownLatch releaseSlow = new CountDownLatch(1);
        for (int i = 0; i < POOL_A; i++) {
            new Thread(() -> {
                bulkheadA.acquireUninterruptibly();
                slowStarted.countDown();
                try { releaseSlow.await(); } catch (InterruptedException ignored) {}
                bulkheadA.release();
            }).start();
        }
        slowStarted.await();

        long start = System.nanoTime();
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                for (int i = 0; i < REQ_PER_THREAD; i++) {
                    if (!bulkheadB.tryAcquire()) {
                        healthyDropped.incrementAndGet();
                    } else {
                        bulkheadB.release();
                    }
                }
                done.countDown();
            }).start();
        }
        done.await();
        releaseSlow.countDown();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("healthyDropped=" + healthyDropped.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
