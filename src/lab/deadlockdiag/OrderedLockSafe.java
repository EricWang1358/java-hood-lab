package lab.deadlockdiag;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderedLockSafe {
    static final Object LOCK_A = new Object();
    static final Object LOCK_B = new Object();

    static void transfer(Object x, Object y, AtomicInteger ok) {
        Object first = System.identityHashCode(x) <= System.identityHashCode(y) ? x : y;
        Object second = first == x ? y : x;
        synchronized (first) {
            synchronized (second) {
                ok.incrementAndGet();
            }
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
