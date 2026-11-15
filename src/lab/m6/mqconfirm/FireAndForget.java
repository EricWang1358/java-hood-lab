package lab.m6.mqconfirm;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class FireAndForget {
    static final int MSGS = 200;
    static final AtomicInteger lostMessages = new AtomicInteger();
    static volatile long sink;

    static void send(int seq) {
        // Producer fires without broker ack; broker crashes or drops 10%; consumer auto-acks before processing
        if (seq % 10 == 0 || seq % 15 == 0) {
            lostMessages.incrementAndGet();
            return;
        }
        sink ^= seq;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] ws = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            ws[t] = new Thread(() -> {
                try { barrier.await(); for (int i = 0; i < MSGS; i++) send(i); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
            ws[t].start();
        }
        for (Thread w : ws) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lostMessages=" + lostMessages.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
