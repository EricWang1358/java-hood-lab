package lab.m6.mqidem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class DedupCacheOnly {
    static final int ORDERS = 50;
    static final ConcurrentHashMap<Integer, Boolean> seen = new ConcurrentHashMap<>();
    static final AtomicInteger chargeCount = new AtomicInteger();

    static void onMessage(int orderId) {
        if (Boolean.TRUE.equals(seen.get(orderId))) return; // Check-then-act race!
        Thread.yield();
        chargeCount.incrementAndGet();
        seen.put(orderId, Boolean.TRUE);
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
                    for (int i = 0; i < ORDERS; i++) onMessage(i);
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            ws[t].start();
        }
        for (Thread w : ws) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        int extra = Math.max(0, chargeCount.get() - ORDERS);
        System.out.println("duplicateCharges=" + extra);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
