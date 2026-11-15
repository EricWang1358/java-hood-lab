package lab.m6.breaker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedPoolCascade {
    static final int SHARED_POOL = 8;
    static final int REQ_PER_THREAD = 20;
    static final Semaphore sharedThreads = new Semaphore(SHARED_POOL);
    static final AtomicInteger healthyDropped = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        // Slow Service A holds all 8 shared Tomcat threads!
        CountDownLatch slowStarted = new CountDownLatch(SHARED_POOL);
        CountDownLatch releaseSlow = new CountDownLatch(1);
        for (int i = 0; i < SHARED_POOL; i++) {
            new Thread(() -> {
                sharedThreads.acquireUninterruptibly();
                slowStarted.countDown();
                try { releaseSlow.await(); } catch (InterruptedException ignored) {}
                sharedThreads.release();
            }).start();
        }
        slowStarted.await();

        long start = System.nanoTime();
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                for (int i = 0; i < REQ_PER_THREAD; i++) {
                    if (!sharedThreads.tryAcquire()) {
                        healthyDropped.incrementAndGet(); // Healthy Service B starved by A!
                    } else {
                        sharedThreads.release();
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
