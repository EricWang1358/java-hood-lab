package lab.m6.mqdelay;

import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SleepWaitThread {
    static final int ORDERS = 200;
    static final PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    static final AtomicInteger scanOps = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        for (int i = 0; i < ORDERS; i++) {
            minHeap.offer(10 + (i % 15));
            scanOps.addAndGet(8); // O(log N) heap insertion
        }
        while (!minHeap.isEmpty()) {
            minHeap.poll();
            scanOps.addAndGet(8);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanOps=" + scanOps.get());
        System.out.println("expected=" + (ORDERS * 2));
        System.out.println("ms=" + ms);
    }
}
