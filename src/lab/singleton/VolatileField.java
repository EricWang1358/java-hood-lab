package lab.singleton;

import java.util.concurrent.atomic.AtomicInteger;

public class VolatileField {
    static final int ORDERS = 50_000;
    static final VolatileField SERVICE = new VolatileField();
    static final AtomicInteger okOrders = new AtomicInteger();

    volatile int pendingPrice;

    static int calcFee(int price) {
        int h = price;
        for (int i = 0; i < 200; i++) h = (h * 31) ^ (h >>> 7) + i;
        return (h & 31) + 10;
    }

    int quote(int basePrice) {
        pendingPrice = basePrice;
        int fee = calcFee(basePrice);
        return pendingPrice + fee;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            int tier = (t + 1) * 1000;
            workers[t] = new Thread(() -> {
                int mine = 0;
                for (int i = 0; i < ORDERS; i++) {
                    int price = tier + (i & 63);
                    int want = price + calcFee(price);
                    if (SERVICE.quote(price) == want) mine++;
                }
                okOrders.addAndGet(mine);
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("okOrders=" + okOrders.get());
        System.out.println("expected=" + (threads * ORDERS));
        System.out.println("ms=" + ms);
    }
}
