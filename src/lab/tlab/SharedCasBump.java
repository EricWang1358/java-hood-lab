package lab.tlab;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class SharedCasBump {
    static final AtomicLong globalTop = new AtomicLong(0);
    static volatile long sink;

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
                long sum = 0;
                for (int i = 0; i < perThread; i++) {
                    sum += globalTop.getAndAdd(24);
                }
                sink = sum;
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
