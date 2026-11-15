package lab.cow;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CopyOnWrite {
    static final int READS = 30_000;
    static final int SIZE = 320;
    static final String[] TAGS = {"rule-a", "rule-b", "rule-c", "rule-d"};
    static List<String> rules;

    static int scanOnce() {
        int sum = 0;
        for (String rule : rules) {
            sum += rule.length();
        }
        return sum;
    }

    static void updateRule(int pick) {
        rules.add(TAGS[pick & 3]);
        rules.remove(rules.size() - 1);
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int readers = threads == 1 ? 1 : threads - 1;
        int expected = readers * READS;

        rules = new CopyOnWriteArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            rules.add(TAGS[i & 3]);
        }

        AtomicInteger okReads = new AtomicInteger();
        long start = System.nanoTime();

        if (threads == 1) {
            int ok = 0;
            for (int i = 0; i < READS; i++) {
                if (scanOnce() > 0) ok++;
                if ((i & 31) == 0) updateRule(i);
            }
            okReads.set(ok);
        } else {
            AtomicBoolean done = new AtomicBoolean(false);
            CountDownLatch ready = new CountDownLatch(readers);
            Thread writer = new Thread(() -> {
                int seq = 0;
                int pace = 1;
                while (!done.get()) {
                    for (int k = 0; k < 2500; k++) pace = pace * 31 + k;
                    updateRule(seq++ ^ (pace & 1));
                }
            });
            writer.start();

            Thread[] workers = new Thread[readers];
            for (int t = 0; t < readers; t++) {
                workers[t] = new Thread(() -> {
                    ready.countDown();
                    int ok = 0;
                    for (int i = 0; i < READS; i++) {
                        if (scanOnce() > 0) ok++;
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
