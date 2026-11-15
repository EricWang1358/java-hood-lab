package lab.lazysingle;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class VolatileDcl {
    static final int WAVES = 100;
    static final int SLOTS = 16;
    static final int READS = 600;
    static final AtomicInteger inits = new AtomicInteger();
    static volatile long sink;

    static final class Heavy {
        final int val;
        Heavy(int val) { this.val = val; }
    }

    private volatile Heavy instance;

    private static Heavy createHeavy() {
        inits.incrementAndGet();
        long x = 17;
        for (int i = 0; i < 12_000; i++) x = x * 31 + i;
        return new Heavy((int) (x & 0xffff));
    }

    Heavy get() {
        Heavy h = instance;
        if (h == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = createHeavy();
                }
                h = instance;
            }
        }
        return h;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        VolatileDcl[] slots = new VolatileDcl[SLOTS];
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> {
            for (int i = 0; i < SLOTS; i++) slots[i] = new VolatileDcl();
        });
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try {
                    long sum = 0;
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        for (int r = 0; r < READS; r++) {
                            for (int i = 0; i < SLOTS; i++) {
                                sum += slots[i].get().val;
                            }
                        }
                    }
                    sink = sum;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("inits=" + inits.get());
        System.out.println("expected=" + (WAVES * SLOTS));
        System.out.println("ms=" + ms);
    }
}
