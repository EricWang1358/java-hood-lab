package lab.tlpool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncMapCtx {
    static final int ROUNDS = 2_000;
    static final Map<Thread, String> CTX = new HashMap<>();
    static final AtomicInteger cleanGuests = new AtomicInteger();
    static volatile int sink;

    static void handleVip(int id) {
        Thread self = Thread.currentThread();
        synchronized (CTX) {
            CTX.put(self, "vip-" + id);
            try {
                work(id, 4_000);
                if (id % 4 == 0) throw new IllegalStateException("bad");
            } finally {
                CTX.remove(self);
            }
        }
    }

    static void handleGuest(int id) {
        synchronized (CTX) {
            work(id, 4_000);
            if (CTX.get(Thread.currentThread()) == null) cleanGuests.incrementAndGet();
        }
    }

    static void work(int seed, int steps) {
        int x = seed;
        for (int i = 0; i < steps; i++) x = x * 31 + i;
        sink = x;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int total = threads * ROUNDS;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            final int id = i;
            pool.execute(() -> {
                try {
                    handleVip(id);
                } catch (RuntimeException ignored) {
                }
            });
            pool.execute(() -> handleGuest(id));
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("cleanGuests=" + cleanGuests.get());
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
