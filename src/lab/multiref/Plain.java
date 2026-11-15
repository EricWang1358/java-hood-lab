package lab.multiref;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Plain {
    static final int READS = 450_000;
    static int discount = 10;
    static int threshold = 50;
    static volatile int sink;

    static int spin(int x, int rounds) {
        for (int i = 0; i < rounds; i++) x = x * 31 + i;
        return x;
    }

    static int writeRule(int d, int pace) {
        discount = d;
        pace = spin(pace, 2_500);
        threshold = d * 5;
        return pace;
    }

    static boolean readOk() {
        int d = discount;
        int t = threshold;
        return t == d * 5;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int readers = threads == 1 ? 1 : threads - 1;
        int expected = readers * READS;
        AtomicInteger okReads = new AtomicInteger();
        long start = System.nanoTime();

        if (threads == 1) {
            int ok = 0, d = 10, pace = 1, w = 1;
            for (int i = 0; i < READS; i++) {
                if (i % 64 == 0) {
                    d = d == 10 ? 20 : 10;
                    pace = writeRule(d, pace);
                }
                if (readOk()) ok++;
                w = spin(w, 40);
            }
            sink = pace ^ w;
            okReads.set(ok);
        } else {
            AtomicBoolean done = new AtomicBoolean(false);
            CountDownLatch started = new CountDownLatch(1);
            Thread writer = new Thread(() -> {
                int d = 10, pace = 1;
                boolean first = true;
                while (!done.get()) {
                    d = d == 10 ? 20 : 10;
                    pace = writeRule(d, pace);
                    if (first) { started.countDown(); first = false; }
                    pace = spin(pace, 7_500);
                }
                sink = pace;
            });
            writer.start();
            started.await();

            Thread[] workers = new Thread[readers];
            for (int t = 0; t < readers; t++) {
                final int seed = t + 1;
                workers[t] = new Thread(() -> {
                    int ok = 0, w = seed;
                    for (int i = 0; i < READS && !done.get(); i++) {
                        if (readOk()) ok++;
                        w = spin(w, 40);
                    }
                    okReads.addAndGet(ok);
                    sink = w;
                });
                workers[t].start();
            }
            for (Thread w : workers) w.join();
            done.set(true);
            writer.join();
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("okReads=" + okReads.get());
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
