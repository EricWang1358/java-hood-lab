package lab.stamped;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWrite {
    static final int READS = 280_000;
    static final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    static volatile int x = 10, y = 10;
    static volatile int sink;

    static boolean readOk() {
        rw.readLock().lock();
        try {
            int a = x;
            int b = y;
            return a == b;
        } finally {
            rw.readLock().unlock();
        }
    }

    static int writePair(int val, int pace) {
        rw.writeLock().lock();
        try {
            x = val;
            for (int k = 0; k < 60; k++) pace = pace * 31 + k;
            y = val;
        } finally {
            rw.writeLock().unlock();
        }
        return pace;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int readers = threads == 1 ? 1 : threads - 1;
        int expected = readers * READS;
        AtomicInteger okReads = new AtomicInteger();
        long start = System.nanoTime();

        if (threads == 1) {
            int ok = 0, val = 10, pace = 1;
            for (int i = 0; i < READS; i++) {
                val = val == 10 ? 20 : 10;
                pace = writePair(val, pace);
                if (readOk()) ok++;
            }
            sink = pace;
            okReads.set(ok);
        } else {
            AtomicBoolean done = new AtomicBoolean(false);
            CountDownLatch ready = new CountDownLatch(readers);
            Thread writer = new Thread(() -> {
                int val = 10, pace = 1;
                while (!done.get()) {
                    for (int k = 0; k < 10; k++) pace = pace * 31 + k;
                    val = val == 10 ? 20 : 10;
                    pace = writePair(val, pace);
                }
                sink = pace;
            });
            writer.start();

            Thread[] workers = new Thread[readers];
            for (int t = 0; t < readers; t++) {
                workers[t] = new Thread(() -> {
                    ready.countDown();
                    int ok = 0;
                    for (int i = 0; i < READS; i++) {
                        if (readOk()) ok++;
                    }
                    okReads.addAndGet(ok);
                });
                workers[t].start();
            }
            ready.await();
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
