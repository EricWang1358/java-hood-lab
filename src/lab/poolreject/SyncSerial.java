package lab.poolreject;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncSerial {
    static final int ROUNDS = 6;
    static final int BURST = 4;
    static final Object LOCK = new Object();
    static final AtomicInteger doneTasks = new AtomicInteger();
    static volatile int sink;

    static void handleOrder(int id) {
        int x = id;
        for (int i = 0; i < 1_100_000; i++) x = x * 31 + i;
        sink = x;
        doneTasks.incrementAndGet();
    }

    static void submitOrder(int id) {
        synchronized (LOCK) {
            handleOrder(id);
        }
    }

    static void runRound(int callers) throws Exception {
        CyclicBarrier gate = new CyclicBarrier(callers);
        Thread[] threads = new Thread[callers];
        for (int t = 0; t < callers; t++) {
            final int base = t * BURST;
            threads[t] = new Thread(() -> {
                try {
                    gate.await();
                    for (int i = 0; i < BURST; i++) submitOrder(base + i);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            threads[t].start();
        }
        for (Thread th : threads) th.join();
    }

    public static void main(String[] args) throws Exception {
        int callers = Integer.parseInt(args[0]);
        doneTasks.set(0);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) runRound(callers);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("doneTasks=" + doneTasks.get());
        System.out.println("expected=" + (ROUNDS * callers * BURST));
        System.out.println("ms=" + ms);
    }
}
