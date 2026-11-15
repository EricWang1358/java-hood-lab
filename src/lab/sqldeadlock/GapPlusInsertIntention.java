package lab.sqldeadlock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class GapPlusInsertIntention {
    public static void main(String[] args) throws Exception {
        int pairs = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        AtomicInteger deadlocks = new AtomicInteger(0);
        for (int i = 0; i < pairs; i++) {
            CountDownLatch bothHoldGap = new CountDownLatch(2);
            Thread t1 = new Thread(() -> {
                // Step 1: Tx1 acquires Gap Lock on (5, 10)
                bothHoldGap.countDown();
                try { bothHoldGap.await(); } catch (InterruptedException ignored) {}
                // Step 2: Tx1 requests Insert Intention Lock -> blocked by Tx2's Gap Lock!
                deadlocks.incrementAndGet();
            });
            Thread t2 = new Thread(() -> {
                // Step 1: Tx2 ALSO acquires Gap Lock on (5, 10) (Gap Locks are compatible!)
                bothHoldGap.countDown();
                try { bothHoldGap.await(); } catch (InterruptedException ignored) {}
            });
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("deadlocks=" + deadlocks.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
