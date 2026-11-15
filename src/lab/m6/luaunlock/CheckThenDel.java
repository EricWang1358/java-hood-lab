package lab.m6.luaunlock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class CheckThenDel {
    static final int ROUNDS = 40;
    static final ConcurrentHashMap<String, String> lock = new ConcurrentHashMap<>();
    static final AtomicInteger stolenLocks = new AtomicInteger();

    static void spin(int n) {
        long x = n;
        for (int i = 0; i < n; i++) x = x * 31 + i;
        if (x == 42) System.out.print("");
    }

    static void checkAndDelete(String owner) {
        String cur = lock.get("k");
        if (owner.equals(cur)) {
            spin(15_000); // GC pause or network jitter between GET and DEL!
            String removed = lock.remove("k");
            if (removed != null && !owner.equals(removed)) {
                stolenLocks.incrementAndGet();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(2);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            lock.put("k", "A");
            Thread a = new Thread(() -> {
                try { barrier.await(); checkAndDelete("A"); } catch (Exception e) { throw new RuntimeException(e); }
            });
            Thread b = new Thread(() -> {
                try {
                    barrier.await();
                    spin(3_000);
                    lock.put("k", "B"); // TTL expired -> B acquires lock before A's DEL arrives
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            a.start(); b.start(); a.join(); b.join();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("stolenLocks=" + stolenLocks.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
