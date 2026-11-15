package lab.deadlockdiag;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadMxBeanDetect {
    static final ReentrantLock LOCK_A = new ReentrantLock();
    static final ReentrantLock LOCK_B = new ReentrantLock();

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        CountDownLatch entered = new CountDownLatch(2);
        Thread t1 = new Thread(() -> {
            LOCK_A.lock();
            try {
                entered.countDown();
                try { entered.await(); LOCK_B.lockInterruptibly(); LOCK_B.unlock(); }
                catch (InterruptedException ignored) {}
            } finally { LOCK_A.unlock(); }
        });
        Thread t2 = new Thread(() -> {
            LOCK_B.lock();
            try {
                entered.countDown();
                try { entered.await(); LOCK_A.lockInterruptibly(); LOCK_A.unlock(); }
                catch (InterruptedException ignored) {}
            } finally { LOCK_B.unlock(); }
        });
        long start = System.nanoTime();
        t1.start();
        t2.start();
        entered.await();
        Thread.sleep(15);
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long[] ids = mx.findDeadlockedThreads();
        boolean detected = ids != null && ids.length >= 2;
        t1.interrupt();
        t1.join();
        t2.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + (detected ? n : 0));
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
