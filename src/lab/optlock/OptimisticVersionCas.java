package lab.optlock;

import java.util.concurrent.atomic.AtomicInteger;

public class OptimisticVersionCas {
    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        AtomicInteger stock = new AtomicInteger(100);
        AtomicInteger version = new AtomicInteger(0);
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int k = 0; k < 20; k++) {
                    while (true) {
                        int s = stock.get();
                        int v = version.get();
                        if (s <= 0) break;
                        if (version.compareAndSet(v, v + 1)) {
                            stock.decrementAndGet();
                            break;
                        }
                    }
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
