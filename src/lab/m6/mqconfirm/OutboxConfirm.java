package lab.m6.mqconfirm;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class OutboxConfirm {
    static final int MSGS = 200;
    static final ConcurrentLinkedQueue<Integer> outbox = new ConcurrentLinkedQueue<>();
    static final AtomicInteger lostMessages = new AtomicInteger();
    static volatile long sink;

    static void saveBizAndOutbox(int seq) {
        // Local DB transaction writes biz + outbox row atomically; relay retries until Broker ACK
        outbox.offer(seq);
        Integer m = outbox.poll();
        if (m != null) {
            long x = m;
            for (int i = 0; i < 500; i++) x = x * 31 + i;
            sink ^= x;
        } else {
            lostMessages.incrementAndGet();
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] ws = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            ws[t] = new Thread(() -> {
                try { barrier.await(); for (int i = 0; i < MSGS; i++) saveBizAndOutbox(i); }
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
