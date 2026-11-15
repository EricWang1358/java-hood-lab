package lab.lockscope;

import java.util.concurrent.atomic.AtomicInteger;

public class Atomic {
    static final int ROUNDS = 500_000;
    static final AtomicInteger total = new AtomicInteger();

    void add() {
        total.incrementAndGet();
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                for (int i = 0; i < ROUNDS; i++) {
                    new Atomic().add();
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("total=" + total.get());
        System.out.println("expected=" + (threads * ROUNDS));
        System.out.println("ms=" + ms);
    }
}
