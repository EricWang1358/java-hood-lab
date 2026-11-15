package lab.stock;

import java.util.concurrent.atomic.AtomicInteger;

public class Sync {
    static int stock = 1_000_000;
    static AtomicInteger sold = new AtomicInteger();

    static synchronized boolean sellOne() {
        if (stock > 0) {
            stock--;
            sold.incrementAndGet();
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int expected = stock;
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
