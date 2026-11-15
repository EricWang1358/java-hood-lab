package lab.virtrace;

import java.util.concurrent.CountDownLatch;

public class PlatformPlain {
    static final int ROUNDS = 1_800_000;
    static int count = 0;

    static void await(CountDownLatch ready) {
        try {
            ready.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CountDownLatch ready = new CountDownLatch(1);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = Thread.ofPlatform().start(() -> {
                await(ready);
                for (int i = 0; i < ROUNDS; i++) {
                    count++;
                }
            });
        }
        ready.countDown();
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("total=" + count);
        System.out.println("expected=" + (threads * ROUNDS));
        System.out.println("ms=" + ms);
    }
}
