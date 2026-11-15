package lab.aba;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Sync {
    static final int WAVES = 250;
    static final int WORK_BONUS = 180_000;
    static final int WORK_USER = 50_000;
    static volatile int sink;
    static int balance = 19;
    static boolean used = false;
    static final AtomicInteger gifts = new AtomicInteger();
    static final AtomicInteger cleanOps = new AtomicInteger();

    static void work(int steps) {
        int x = 1;
        for (int i = 0; i < steps; i++) x = x * 31 + i;
        sink = x;
    }

    static synchronized void grantBonus() {
        if (balance == 19 && !used) {
            work(WORK_BONUS);
            balance = balance + 20;
            used = true;
            gifts.incrementAndGet();
        }
    }

    static void spendAndTopUp() {
        work(WORK_USER);
        synchronized (Sync.class) {
            if (balance == 19 && !used) {
                balance = 9;
                used = true;
                gifts.incrementAndGet();
                balance = 19;
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
                used = false;
                balance = 19;
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
