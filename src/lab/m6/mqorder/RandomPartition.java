package lab.m6.mqorder;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomPartition {
    static final int ORDERS = 60;
    static final AtomicInteger[] orderStep = new AtomicInteger[ORDERS];
    static final AtomicInteger outOfOrder = new AtomicInteger();

    static {
        for (int i = 0; i < ORDERS; i++) orderStep[i] = new AtomicInteger(0);
    }

    static void consume(int orderId, int step) {
        int prev = orderStep[orderId].getAndSet(step);
        if (prev != step - 1) outOfOrder.incrementAndGet(); // step 2/3 arrived before step 1!
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
                    for (int i = 0; i < ORDERS; i++) {
                        int step = (i + tid) % 3 + 1; // random partition delivery scrambles steps 1,2,3
                        consume(i, step);
                    }
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
