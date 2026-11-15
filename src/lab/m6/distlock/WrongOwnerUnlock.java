package lab.m6.distlock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class WrongOwnerUnlock {
    static final int ROUNDS = 30;
    static final ConcurrentHashMap<String, String> lockTable = new ConcurrentHashMap<>();
    static final AtomicInteger lockViolations = new AtomicInteger();

    static void runRound(String owner) {
        if (lockTable.putIfAbsent("lock:order", owner) == null) {
            if ("t-0".equals(owner)) {
                // Simulate TTL expiry: another owner acquired the lock
                lockTable.put("lock:order", "t-next");
            }
            // Blindly calling DEL without checking owner!
            String removed = lockTable.remove("lock:order");
            if (removed != null && !removed.equals(owner)) {
                lockViolations.incrementAndGet();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            lockTable.clear();
            Thread[] ws = new Thread[threads];
            for (int t = 0; t < threads; t++) {
                final String owner = "t-" + t;
                ws[t] = new Thread(() -> {
                    try { barrier.await(); runRound(owner); } catch (Exception e) { throw new RuntimeException(e); }
                });
                ws[t].start();
            }
            for (Thread w : ws) w.join();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lockViolations=" + lockViolations.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
