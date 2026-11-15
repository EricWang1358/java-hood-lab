package lab.optlock;

import java.util.concurrent.atomic.AtomicInteger;

public class DirectSqlAtomicCheck {
    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        AtomicInteger stock = new AtomicInteger(100);
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int k = 0; k < 20; k++) {
                    // UPDATE t SET stock = stock - 1 WHERE id = ? AND stock >= 1
                    stock.updateAndGet(cur -> cur > 0 ? cur - 1 : cur);
                }
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        int oversell = Math.max(0, -stock.get());
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("oversell=" + oversell);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
