package lab.tlab;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class LargeArrayBypassTlab {
    static volatile byte[] sink;

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        int threads = 4;
        int perThread = n / threads;
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicLong total = new AtomicLong();

        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                try { ready.await(); } catch (InterruptedException ignored) {}
                byte[] last = null;
                for (int i = 0; i < perThread; i++) {
                    if ((i & 31) == 0) {
                        last = new byte[32768];
                    }
                }
                sink = last;
                total.addAndGet(perThread);
                done.countDown();
            }).start();
        }
        long start = System.nanoTime();
        ready.countDown();
        done.await();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("allocated=" + total.get());
        System.out.println("expected=" + (perThread * threads));
        System.out.println("ms=" + ms);
    }
}
