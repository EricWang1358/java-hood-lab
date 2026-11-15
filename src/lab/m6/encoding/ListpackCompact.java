package lab.m6.encoding;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

public class ListpackCompact {
    static final int OBJECTS = 400;
    static final int FIELDS = 20;
    static final AtomicLong memBytes = new AtomicLong();
    static volatile long sink;

    static void runBatch() {
        byte[][] listpacks = new byte[OBJECTS][];
        long bytes = 0;
        for (int i = 0; i < OBJECTS; i++) {
            // Contiguous compact buffer (listpack): no pointers, ~12 bytes per field+val
            listpacks[i] = new byte[16 + FIELDS * 12];
            bytes += listpacks[i].length;
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
