package lab.stock;

import java.util.concurrent.atomic.AtomicInteger;

public class Atomic {
    static AtomicInteger stock = new AtomicInteger(1_000_000);
    static AtomicInteger sold = new AtomicInteger();

    static boolean sellOne() {
        if (stock.get() > 0) {
            stock.decrementAndGet();
            sold.incrementAndGet();
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int expected = stock.get();
        Thread[] sellers = new Thread[threads];
        long start = System.nanoTime();

        for (int i = 0; i < threads; i++) {
            sellers[i] = new Thread(() -> {
                while (sellOne()) {
                }
            });
            sellers[i].start();
        }
        for (Thread seller : sellers) {
            seller.join();
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("sold=" + sold.get());
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
