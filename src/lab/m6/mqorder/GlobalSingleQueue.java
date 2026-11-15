package lab.m6.mqorder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class GlobalSingleQueue {
    static final int ORDERS = 60;
    static final ReentrantLock singleQueueLock = new ReentrantLock();
    static final AtomicInteger[] orderStep = new AtomicInteger[ORDERS];
    static final AtomicInteger outOfOrder = new AtomicInteger();
    static volatile long sink;

    static {
        for (int i = 0; i < ORDERS; i++) orderStep[i] = new AtomicInteger(0);
    }

    static void consumeSequential(int orderId, int step) {
        singleQueueLock.lock();
        try {
            int prev = orderStep[orderId].getAndSet(step);
            if (prev != step - 1) outOfOrder.incrementAndGet();
            long x = orderId;
            for (int i = 0; i < 4_000; i++) x = x * 31 + i;
            sink ^= x;
        } finally {
            singleQueueLock.unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        Thread singleConsumer = new Thread(() -> {
            for (int i = 0; i < ORDERS; i++) {
                for (int s = 1; s <= 3; s++) consumeSequential(i, s);
            }
        });
        singleConsumer.start();
        singleConsumer.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("outOfOrder=" + outOfOrder.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
