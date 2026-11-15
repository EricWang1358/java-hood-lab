package lab.m6.consistency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class DbFirstThenCache {
    static final int ROUNDS = 80;
    static final ConcurrentHashMap<String, Integer> db = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, Integer> cache = new ConcurrentHashMap<>();
    static final AtomicInteger staleReads = new AtomicInteger();

    static void spin(int n) {
        long x = n;
        for (int i = 0; i < n; i++) x = x * 31 + i;
        if (x == 42) System.out.print("");
    }

    static void writeUpdate(int newVal) {
        spin(5_000);
        db.put("k", newVal);
        spin(10_000);
        cache.remove("k");
    }

    static int readQuery() {
        Integer v = cache.get("k");
        if (v != null) return v;
        int dbVal = db.get("k");
        spin(25_000);
        cache.put("k", dbVal);
        return dbVal;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int readers = Math.max(1, threads - 1);
        CyclicBarrier barrier = new CyclicBarrier(readers + 1);
        long start = System.nanoTime();
        for (int r = 1; r <= ROUNDS; r++) {
            final int val = r;
            db.put("k", r - 1);
            cache.remove("k");
            Thread writer = new Thread(() -> {
                try { barrier.await(); writeUpdate(val); } catch (Exception e) { throw new RuntimeException(e); }
            });
            Thread[] rs = new Thread[readers];
            for (int i = 0; i < readers; i++) {
                rs[i] = new Thread(() -> {
                    try { barrier.await(); readQuery(); } catch (Exception e) { throw new RuntimeException(e); }
                });
                rs[i].start();
            }
            writer.start();
            writer.join();
            for (Thread t : rs) t.join();
            Integer cached = cache.get("k");
            if (cached != null && !cached.equals(db.get("k"))) staleReads.incrementAndGet();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("staleReads=" + staleReads.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
