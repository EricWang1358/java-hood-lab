package lab.deadlockdiag;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TryLockTimeoutEscape {
    static final ReentrantLock LOCK_A = new ReentrantLock();
    static final ReentrantLock LOCK_B = new ReentrantLock();

    static void transfer(ReentrantLock first, ReentrantLock second, AtomicInteger ok) {
        while (true) {
            if (first.tryLock()) {
                try {
                    try {
                        if (second.tryLock(2, TimeUnit.MILLISECONDS)) {
                            try {
                                ok.incrementAndGet();
                                return;
                            } finally {
                                second.unlock();
                            }
                        }
                    } catch (InterruptedException ignored) {}
                } finally {
                    first.unlock();
                }
            }
            Thread.yield();
        }
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        int half = n / 2;
        AtomicInteger ok = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(2);
        long start = System.nanoTime();
        new Thread(() -> { for (int i = 0; i < half; i++) transfer(LOCK_A, LOCK_B, ok); done.countDown(); }).start();
        new Thread(() -> { for (int i = 0; i < n - half; i++) transfer(LOCK_B, LOCK_A, ok); done.countDown(); }).start();
        done.await();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok.get());
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
