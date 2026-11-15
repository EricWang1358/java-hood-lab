package lab.m6.mqbacklog;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadPull {
    static final int BURST = 600;
    static final ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(2000);
    static final AtomicInteger backlogPeak = new AtomicInteger();

    static void work() {
        long x = 1;
        for (int i = 0; i < 400; i++) x = (x * 31 + i) & 0xFFFF;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        for (int i = 0; i < BURST; i++) queue.offer(i);
        backlogPeak.set(queue.size());

        long start = System.nanoTime();
        // Ignores available consumer concurrency: single consumer thread drains sequentially
        Thread single = new Thread(() -> {
            while (!queue.isEmpty()) {
                if (queue.poll() != null) work();
                int sz = queue.size();
                backlogPeak.accumulateAndGet(sz, Math::max);
            }
        });
        single.start();
        single.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backlogPeak=" + backlogPeak.get());
        System.out.println("expected=150");
        System.out.println("ms=" + ms);
    }
}
