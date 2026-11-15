package lab.m6.mqidem;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class NaiveConsume {
    static final int ORDERS = 50;
    static final AtomicInteger balance = new AtomicInteger(1000);
    static final AtomicInteger duplicateCharges = new AtomicInteger();

    static void onMessage(int orderId) {
        balance.addAndGet(-10);
        duplicateCharges.incrementAndGet();
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        long start = System.nanoTime();
        Thread[] ws = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            ws[t] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int i = 0; i < ORDERS; i++) onMessage(i); // redelivered across retries
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            ws[t].start();
        }
        for (Thread w : ws) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        int extra = Math.max(0, duplicateCharges.get() - ORDERS);
        System.out.println("duplicateCharges=" + extra);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
