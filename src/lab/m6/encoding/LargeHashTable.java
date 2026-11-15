package lab.m6.encoding;

import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

public class LargeHashTable {
    static final int OBJECTS = 400;
    static final int FIELDS = 20;
    static final AtomicLong memBytes = new AtomicLong();
    static volatile long sink;

    static void runBatch() {
        HashMap<String, HashMap<String, String>> table = new HashMap<>(OBJECTS);
        long bytes = 0;
        for (int i = 0; i < OBJECTS; i++) {
            HashMap<String, String> inner = new HashMap<>(FIELDS);
            bytes += 64; // dict header
            for (int f = 0; f < FIELDS; f++) {
                inner.put("f" + f, "v" + f);
                bytes += 48; // dictEntry + SDS per field in hashtable encoding
            }
            table.put("u:" + i, inner);
        }
        memBytes.addAndGet(bytes);
        sink ^= table.size();
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
