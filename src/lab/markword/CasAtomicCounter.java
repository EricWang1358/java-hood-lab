package lab.markword;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class CasAtomicCounter {
    static final AtomicLong count = new AtomicLong(0);

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        int threads = 4;
        int per = n / threads;
        count.set(0);
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                try { ready.await(); } catch (InterruptedException ignored) {}
                for (int i = 0; i < per; i++) {
                    count.incrementAndGet();
                }
                done.countDown();
            }).start();
        }
        long start = System.nanoTime();
        ready.countDown();
        done.await();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("count=" + count.get());
        System.out.println("expected=" + (per * threads));
        System.out.println("ms=" + ms);
    }
}
