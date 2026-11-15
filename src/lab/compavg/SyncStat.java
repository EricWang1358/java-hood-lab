package lab.compavg;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncStat {
    static final int READS = 450_000;
    static final Object LOCK = new Object();
    static long count = 0;
    static long sum = 0;
    static volatile int sink;

    static int spin(int x, int rounds) {
        for (int i = 0; i < rounds; i++) x = x * 31 + i;
        return x;
    }

    static int record(int pace) {
        synchronized (LOCK) {
            count++;
            pace = spin(pace, 2_500);
            sum += 10;
            return pace;
        }
    }

    static boolean readOk() {
        synchronized (LOCK) {
            long c = count;
            long s = sum;
            return s == c * 10;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int readers = threads == 1 ? 1 : threads - 1;
        int expected = readers * READS;
        AtomicInteger okReads = new AtomicInteger();
        long start = System.nanoTime();

        if (threads == 1) {
            int ok = 0, pace = 1, w = 1;
            for (int i = 0; i < READS; i++) {
                if (i % 64 == 0) pace = record(pace);
                if (readOk()) ok++;
                w = spin(w, 40);
            }
            sink = pace ^ w;
            okReads.set(ok);
        } else {
            AtomicBoolean done = new AtomicBoolean(false);
            CountDownLatch started = new CountDownLatch(1);
            Thread writer = new Thread(() -> {
                int pace = 1;
                boolean first = true;
                while (!done.get()) {
                    pace = record(pace);
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
