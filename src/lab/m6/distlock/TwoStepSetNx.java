package lab.m6.distlock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class TwoStepSetNx {
    static final int ROUNDS = 30;
    static final ConcurrentHashMap<String, Long> lockTable = new ConcurrentHashMap<>();
    static final AtomicInteger lockViolations = new AtomicInteger();

    static void runRound(int tid, boolean crashBeforeExpire) {
        if (lockTable.putIfAbsent("lock:order", 1L) == null) {
            if (crashBeforeExpire) {
                // Crashed before EXPIRE! Lock has no TTL -> permanent deadlock
                return;
            }
            lockTable.put("lock:order", System.nanoTime() + 1_000_000L);
            lockTable.remove("lock:order");
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            lockTable.clear();
            final int round = r;
            Thread[] ws = new Thread[threads];
            for (int t = 0; t < threads; t++) {
                final int tid = t;
                ws[t] = new Thread(() -> {
                    try {
                        barrier.await();
                        runRound(tid, round % 3 == 0 && tid == 0);
                    } catch (Exception e) { throw new RuntimeException(e); }
                });
                ws[t].start();
            }
            for (Thread w : ws) w.join();
            if (lockTable.containsKey("lock:order")) lockViolations.incrementAndGet();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lockViolations=" + lockViolations.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
