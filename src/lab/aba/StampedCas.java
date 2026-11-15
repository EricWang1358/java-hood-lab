package lab.aba;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

public class StampedCas {
    static final int WAVES = 250;
    static final int WORK_BONUS = 180_000;
    static final int WORK_USER = 50_000;
    static volatile int sink;
    static final AtomicStampedReference<Integer> balance = new AtomicStampedReference<>(19, 0);
    static final AtomicBoolean used = new AtomicBoolean();
    static final AtomicInteger gifts = new AtomicInteger();
    static final AtomicInteger cleanOps = new AtomicInteger();

    static void work(int steps) {
        int x = 1;
        for (int i = 0; i < steps; i++) x = x * 31 + i;
        sink = x;
    }

    static void grantBonus() {
        while (true) {
            int[] stamp = new int[1];
            int cur = balance.get(stamp);
            int s = stamp[0];
            if (cur != 19 || used.get()) return;
            work(WORK_BONUS);
            if (balance.compareAndSet(cur, cur + 20, s, s + 1)) {
                used.set(true);
                gifts.incrementAndGet();
                return;
            }
        }
    }

    static void spendAndTopUp() {
        work(WORK_USER);
        while (true) {
            int[] stamp = new int[1];
            int cur = balance.get(stamp);
            int s = stamp[0];
            if (cur != 19 || used.get()) return;
            if (balance.compareAndSet(19, 9, s, s + 1)) {
                used.set(true);
                gifts.incrementAndGet();
                balance.compareAndSet(9, 19, s + 1, s + 2);
                return;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        AtomicInteger wave = new AtomicInteger();
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> {
            if (wave.getAndIncrement() > 0) {
                if (gifts.get() == 1) cleanOps.incrementAndGet();
                gifts.set(0);
                used.set(false);
                balance.set(19, 0);
            }
        });
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            boolean isBonus = (t == 0);
            workers[t] = new Thread(() -> {
                try {
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        if (isBonus) grantBonus();
                        else spendAndTopUp();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        if (gifts.get() == 1) cleanOps.incrementAndGet();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("cleanOps=" + cleanOps.get());
        System.out.println("expected=" + WAVES);
        System.out.println("ms=" + ms);
    }
}
