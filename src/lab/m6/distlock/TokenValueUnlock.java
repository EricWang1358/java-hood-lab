package lab.m6.distlock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenValueUnlock {
    static final int ROUNDS = 30;
    static final ConcurrentHashMap<String, String> lockTable = new ConcurrentHashMap<>();
    static final AtomicInteger lockViolations = new AtomicInteger();

    static boolean tryLockNxPx(String key, String token) {
        return lockTable.putIfAbsent(key, token) == null;
    }

    static boolean atomicUnlock(String key, String token) {
        return lockTable.remove(key, token); // Atomic compare-and-delete (Lua equivalent)
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            lockTable.clear();
            Thread[] ws = new Thread[threads];
            for (int t = 0; t < threads; t++) {
                final String token = "uuid-" + r + "-" + t;
                ws[t] = new Thread(() -> {
                    try {
                        barrier.await();
                        if (tryLockNxPx("lock:order", token)) {
                            if (token.endsWith("-0")) lockTable.put("lock:order", "uuid-next");
                            boolean ok = atomicUnlock("lock:order", token);
                            if (ok && "uuid-next".equals(lockTable.get("lock:order"))) {
                                lockViolations.incrementAndGet();
                            }
                        }
                    } catch (Exception e) { throw new RuntimeException(e); }
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
