package lab.m6.mqbacklog;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class UnboundedPoolOom {
    static final int BURST = 600;
    static final int SAFE_MEM_LIMIT = 150;
    static final ConcurrentLinkedQueue<byte[]> unboundedHeapQueue = new ConcurrentLinkedQueue<>();
    static final AtomicInteger backlogPeak = new AtomicInteger();

    static void work() {
        long x = 1;
        for (int i = 0; i < 400; i++) x = (x * 31 + i) & 0xFFFF;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        // Dumps all burst messages into an unbounded in-memory queue with 4KB payload each
        for (int i = 0; i < BURST; i++) {
            unboundedHeapQueue.add(new byte[4096]);
            int sz = unboundedHeapQueue.size();
            backlogPeak.accumulateAndGet(sz, Math::max);
        }

        CountDownLatch latch = new CountDownLatch(threads);
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                while (unboundedHeapQueue.poll() != null) {
                    work();
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backlogPeak=" + backlogPeak.get());
        System.out.println("expected=" + SAFE_MEM_LIMIT);
        System.out.println("ms=" + ms);
    }
}
