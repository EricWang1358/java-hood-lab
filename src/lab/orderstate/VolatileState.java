package lab.orderstate;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class VolatileState {
    static final int CREATED = 0, PAID = 1, CANCELED = 2;
    static final int WAVES = 100, BATCH = 160;
    static volatile int sink;

    volatile int state = CREATED;
    final AtomicInteger wins = new AtomicInteger();

    boolean tryTransition(int next) {
        if (state == CREATED) {
            work(150);
            state = next;
            return true;
        }
        return false;
    }

    static void work(int steps) {
        int x = 1;
        for (int i = 0; i < steps; i++) x = x * 31 + i;
        sink = x;
    }

    static void handle(VolatileState order, int next) {
        work(150);
        if (order.tryTransition(next)) {
            work(10_500);
            order.wins.incrementAndGet();
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        VolatileState[] batch = new VolatileState[BATCH];
        for (int i = 0; i < BATCH; i++) batch[i] = new VolatileState();
        AtomicInteger cleanOrders = new AtomicInteger();
        AtomicInteger wave = new AtomicInteger();
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> {
            if (wave.getAndIncrement() > 0) {
                for (VolatileState o : batch) {
                    if (o.wins.getAndSet(0) == 1) cleanOrders.incrementAndGet();
                    o.state = CREATED;
                }
            }
        });
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            int tid = t;
            workers[t] = new Thread(() -> {
                try {
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        if (threads == 1) {
                            for (int i = 0; i < BATCH; i++) {
                                handle(batch[i], (i & 1) == 0 ? PAID : CANCELED);
                                handle(batch[i], (i & 1) == 0 ? CANCELED : PAID);
                            }
                        } else {
                            int slice = BATCH / (threads / 2);
                            int from = (tid / 2) * slice;
                            int next = (tid & 1) == 0 ? PAID : CANCELED;
                            for (int i = from; i < from + slice; i++) handle(batch[i], next);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        for (VolatileState o : batch) if (o.wins.get() == 1) cleanOrders.incrementAndGet();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("cleanOrders=" + cleanOrders.get());
        System.out.println("expected=" + (WAVES * BATCH));
        System.out.println("ms=" + ms);
    }
}
