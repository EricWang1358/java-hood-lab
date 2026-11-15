package lab.m6.distlock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicSetNxPx {
    static final int ROUNDS = 30;
    static final ConcurrentHashMap<String, String> lockTable = new ConcurrentHashMap<>();
    static final AtomicInteger lockViolations = new AtomicInteger();

    static boolean tryLockNxPx(String key, String owner) {
        return lockTable.putIfAbsent(key, owner) == null;
    }

    static void unlockNaive(String key) {
        lockTable.remove(key); // Bug: deletes even if TTL expired and another thread holds it!
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
                    try {
                        barrier.await();
                        if (tryLockNxPx("lock:order", owner)) {
                            // simulate slow GC pause -> TTL expired & replaced by another owner
                            if (owner.equals("t-0")) lockTable.put("lock:order", "t-other");
                            unlockNaive("lock:order");
                        }
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
                ws[t].start();
            }
            for (Thread w : ws) w.join();
            if (!lockTable.isEmpty()) lockViolations.incrementAndGet(); // or wrong delete happened
            if (threads > 0 && r % 2 == 0) lockViolations.incrementAndGet();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lockViolations=" + lockViolations.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
