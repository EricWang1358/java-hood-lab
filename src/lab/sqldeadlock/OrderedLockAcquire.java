package lab.sqldeadlock;

import java.util.concurrent.locks.ReentrantLock;

public class OrderedLockAcquire {
    public static void main(String[] args) throws Exception {
        int pairs = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        ReentrantLock row1 = new ReentrantLock();
        ReentrantLock row2 = new ReentrantLock();
        int deadlocks = 0;
        for (int i = 0; i < pairs; i++) {
            Thread t1 = new Thread(() -> {
                row1.lock();
                try { row2.lock(); row2.unlock(); } finally { row1.unlock(); }
            });
            Thread t2 = new Thread(() -> {
                row1.lock();
                try { row2.lock(); row2.unlock(); } finally { row1.unlock(); }
            });
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("deadlocks=" + deadlocks);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
