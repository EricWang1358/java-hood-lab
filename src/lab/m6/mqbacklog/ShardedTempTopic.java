package lab.m6.mqbacklog;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ShardedTempTopic {
    static final int BURST = 600;
    static final int SHARDS = 16;
    static final int WINDOW = 150;
    @SuppressWarnings("unchecked")
    static final ArrayBlockingQueue<Integer>[] tempTopics = new ArrayBlockingQueue[SHARDS];
    static final AtomicInteger inFlightMem = new AtomicInteger();
    static final AtomicInteger backlogPeak = new AtomicInteger();

    static void work() {
        long x = 1;
        for (int i = 0; i < 400; i++) x = (x * 31 + i) & 0xFFFF;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        for (int s = 0; s < SHARDS; s++) tempTopics[s] = new ArrayBlockingQueue<>(128);
        // Dispatch backlog across 16 temporary topic partitions for parallel drain
        for (int i = 0; i < BURST; i++) tempTopics[i % SHARDS].offer(i);

        CountDownLatch latch = new CountDownLatch(threads);
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            new Thread(() -> {
                for (int s = tid; s < SHARDS; s += threads) {
                    while (tempTopics[s].poll() != null) {
                        int cur = inFlightMem.incrementAndGet();
                        backlogPeak.accumulateAndGet(cur, Math::max);
                        work();
                        inFlightMem.decrementAndGet();
                    }
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backlogPeak=" + backlogPeak.get());
        System.out.println("expected=" + WINDOW);
        System.out.println("ms=" + ms);
    }
}
