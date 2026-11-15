package lab.m6.encoding;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

public class BucketedHash {
    static final int OBJECTS = 400;
    static final int FIELDS = 20;
    static final int BUCKET_CAP = 512;
    static final AtomicLong memBytes = new AtomicLong();
    static volatile long sink;

    static void runBatch() {
        // Pack small sub-hashes under hash-max-listpack-entries (<= 512)
        int buckets = (OBJECTS * FIELDS) / BUCKET_CAP + 1;
        byte[][] listpacks = new byte[buckets][];
        long bytes = 0;
        for (int b = 0; b < buckets; b++) {
            listpacks[b] = new byte[16 + BUCKET_CAP * 12];
            bytes += listpacks[b].length;
        }
        memBytes.addAndGet(bytes);
        sink ^= listpacks.length;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try { barrier.await(); runBatch(); } catch (Exception e) { throw new RuntimeException(e); }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("memKB=" + (memBytes.get() / 1024));
        System.out.println("expected=" + (threads * 220));
        System.out.println("ms=" + ms);
    }
}
