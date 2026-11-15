package lab.queuedrain;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicDrain {
    static final int PER_THREAD = 28_000;
    static final int BATCH_MAX = 256;
    static final String[] LOGS = {"audit-login", "audit-pay", "audit-edit", "audit-export"};
    static final Queue<String> queue = new ConcurrentLinkedQueue<>();
    static volatile boolean done = false;
    static volatile long sink;

    static void enqueue(String log) {
        queue.offer(log);
    }

    static int drainBatch(List<String> batch) {
        batch.clear();
        for (int i = 0; i < BATCH_MAX; i++) {
            String log = queue.poll();
            if (log == null) break;
            batch.add(log);
        }
        if (batch.isEmpty()) return 0;
        return persist(batch);
    }

    static int spin(int x, int rounds) {
        for (int i = 0; i < rounds; i++) x = x * 31 + i;
        return x;
    }

    static int persist(List<String> batch) {
        long h = 0;
        for (String s : batch) h = h * 31 + s.length();
        sink = spin((int) h, 1_100 + batch.size() * 16);
        return batch.size();
    }

    static int runSingle() {
        List<String> batch = new ArrayList<>(BATCH_MAX);
        int total = 0, w = 1;
        for (int i = 0; i < PER_THREAD; i++) {
            w = spin(w, 520);
            enqueue(LOGS[i & 3]);
            if ((i + 1) % BATCH_MAX == 0) total += drainBatch(batch);
        }
        total += drainBatch(batch);
        sink = w;
        return total;
    }

    static int runConcurrent(int threads) throws Exception {
        done = false;
        AtomicInteger saved = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(1);
        Thread collector = new Thread(() -> {
            List<String> batch = new ArrayList<>(BATCH_MAX);
            int total = 0;
            ready.countDown();
            while (!done) {
                int n = drainBatch(batch);
                if (n > 0) total += n;
                else Thread.onSpinWait();
            }
            int tail;
            while ((tail = drainBatch(batch)) > 0) total += tail;
            saved.set(total);
        });
        collector.start();
        ready.await();

        Thread[] producers = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            final int seed = t + 1;
            producers[t] = new Thread(() -> {
                int w = seed;
                for (int i = 0; i < PER_THREAD; i++) {
                    w = spin(w, 520);
                    enqueue(LOGS[i & 3]);
                }
                sink = w;
            });
            producers[t].start();
        }
        for (Thread p : producers) p.join();
        done = true;
        collector.join();
        return saved.get();
    }

    public static void main(String[] args) throws Exception {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        int expected = threads * PER_THREAD;
        long start = System.nanoTime();
        int saved = (threads == 1) ? runSingle() : runConcurrent(threads);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("saved=" + saved);
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
