package lab.m6.mqorder;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class KeyHashPartition {
    static final int ORDERS = 60;
    static final AtomicInteger[] orderStep = new AtomicInteger[ORDERS];
    static final AtomicInteger outOfOrder = new AtomicInteger();
    static volatile long sink;

    static {
        for (int i = 0; i < ORDERS; i++) orderStep[i] = new AtomicInteger(0);
    }

    static void consumeOrder(int orderId) {
        for (int s = 1; s <= 3; s++) {
            int prev = orderStep[orderId].getAndSet(s);
            if (prev != s - 1) outOfOrder.incrementAndGet();
            long x = orderId + s;
            for (int i = 0; i < 4_000; i++) x = x * 31 + i;
            sink ^= x;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        long start = System.nanoTime();
        Thread[] ws = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            ws[t] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int i = tid; i < ORDERS; i += threads) consumeOrder(i); // orderId % partitions == tid
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            ws[t].start();
        }
        for (Thread w : ws) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("outOfOrder=" + outOfOrder.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
