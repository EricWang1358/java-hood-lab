package lab.m6.rpcretry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class HedgedIdempotentRetry {
    static final int CALLS = 60;
    static final ConcurrentHashMap<Integer, Boolean> idemDedup = new ConcurrentHashMap<>();
    static final AtomicInteger failedCalls = new AtomicInteger();

    static boolean idempotentCall(int reqId, int node) {
        if (node == 0 && reqId % 5 == 0) return false; // Node 0 transient timeout
        idemDedup.putIfAbsent(reqId, Boolean.TRUE); // Idempotent key ensures at-most-once effect
        return true;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] ws = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            ws[t] = new Thread(() -> {
                try {
                    barrier.await();
                    for (int i = 0; i < CALLS; i++) {
                        int reqId = tid * CALLS + i;
                        boolean ok = idempotentCall(reqId, 0);
                        if (!ok) {
                            // Failover retry to replica node 1 with same idempotency key
                            ok = idempotentCall(reqId, 1);
                        }
                        if (!ok) failedCalls.incrementAndGet();
                    }
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            ws[t].start();
        }
        for (Thread w : ws) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("failedCalls=" + failedCalls.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
