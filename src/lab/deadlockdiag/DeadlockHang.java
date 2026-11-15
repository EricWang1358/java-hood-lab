package lab.deadlockdiag;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DeadlockHang {
    static final Object A = new Object();
    static final Object B = new Object();

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        CountDownLatch done = new CountDownLatch(2);
        Thread t1 = new Thread(() -> {
            synchronized (A) {
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                synchronized (B) { done.countDown(); }
            }
        });
        Thread t2 = new Thread(() -> {
            synchronized (B) {
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
                synchronized (A) { done.countDown(); }
            }
        });
        t1.setDaemon(true);
        t2.setDaemon(true);
        long start = System.nanoTime();
        t1.start();
        t2.start();
        boolean finished = done.await(50, TimeUnit.MILLISECONDS);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + (finished ? n : 0));
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
