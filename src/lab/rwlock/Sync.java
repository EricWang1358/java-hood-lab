package lab.rwlock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Sync {
    static final int READS_PER_THREAD = 5_000;
    static final Object LOCK = new Object();
    static int basePrice = 100;
    static int discount = 10;
    static volatile int sink;

    static int spin(int x, int rounds) {
        for (int i = 0; i < rounds; i++) {
            x = x * 31 + i;
        }
        return x;
    }

    static int writePrice(boolean high) {
        int price = high ? 200 : 100;
        int disc = high ? 20 : 10;
        synchronized (LOCK) {
            basePrice = price;
            int gap = spin(price, 2_500);
            discount = disc;
            return gap;
        }
    }

    static boolean readValid(int seed, int[] work) {
        synchronized (LOCK) {
            int base = basePrice;
            int disc = discount;
            work[0] ^= spin(base + disc + seed, 6_000);
            return base == disc * 10;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        AtomicInteger okReads = new AtomicInteger();
        AtomicBoolean done = new AtomicBoolean(false);
        long start = System.nanoTime();
        if (threads == 1) {
            int ok = 0;
            int[] work = new int[1];
            for (int i = 0; i < READS_PER_THREAD; i++) {
                work[0] ^= writePrice((i & 1) == 0);
                if (readValid(i, work)) ok++;
            }
            okReads.addAndGet(ok);
            sink ^= work[0];
        } else {
            CountDownLatch started = new CountDownLatch(1);
            Thread writer = new Thread(() -> {
                int w = 0;
                boolean high = false;
                boolean first = true;
                while (!done.get()) {
                    high = !high;
                    w ^= writePrice(high);
                    if (first) {
                        started.countDown();
                        first = false;
                    }
                    w ^= spin(w, 150_000);
                }
                sink ^= w;
            });
            writer.start();
            started.await();
            int readersCount = threads - 1;
            Thread[] readers = new Thread[readersCount];
            for (int t = 0; t < readersCount; t++) {
                final int tid = t;
                readers[t] = new Thread(() -> {
                    int ok = 0;
                    int[] work = new int[1];
                    for (int i = 0; i < READS_PER_THREAD; i++) {
                        if (readValid(i + tid, work)) ok++;
                    }
                    okReads.addAndGet(ok);
                    sink ^= work[0];
                });
                readers[t].start();
            }
            for (Thread r : readers) r.join();
            done.set(true);
            writer.join();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        int expected = (threads == 1 ? 1 : threads - 1) * READS_PER_THREAD;
        System.out.println("okReads=" + okReads.get());
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
