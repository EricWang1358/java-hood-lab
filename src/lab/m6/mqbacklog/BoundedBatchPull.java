package lab.m6.mqbacklog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class BoundedBatchPull {
    static final int BURST = 600;
    static final int WINDOW = 150;
    static final ArrayBlockingQueue<Integer> brokerDiskLog = new ArrayBlockingQueue<>(2000);
    static final ArrayBlockingQueue<Integer> memWindow = new ArrayBlockingQueue<>(WINDOW);
    static final AtomicInteger backlogPeak = new AtomicInteger();

    static void workBatch(int n) {
        long x = 1;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < 400; i++) x = (x * 31 + i) & 0xFFFF;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        for (int i = 0; i < BURST; i++) brokerDiskLog.offer(i);

        CountDownLatch latch = new CountDownLatch(threads);
        long start = System.nanoTime();
        // Pulls from broker into a bounded memory window in batches of 32
        for (int t = 0; t < threads; t++) {
            new Thread(() -> {
                List<Integer> batch = new ArrayList<>(32);
                while (true) {
                    batch.clear();
                    for (int k = 0; k < 32; k++) {
                        Integer m = brokerDiskLog.poll();
                        if (m == null) break;
                        if (memWindow.offer(m)) batch.add(m);
                    }
                    if (batch.isEmpty()) break;
                    backlogPeak.accumulateAndGet(memWindow.size(), Math::max);
                    workBatch(batch.size());
                    for (int k = 0; k < batch.size(); k++) memWindow.poll();
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
