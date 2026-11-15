package lab.m6.luaunlock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class FencingTokenLock {
    static final int ROUNDS = 40;
    static final ConcurrentHashMap<String, String> lock = new ConcurrentHashMap<>();
    static final AtomicLong fencingSeq = new AtomicLong();
    static final AtomicLong dbMaxToken = new AtomicLong();
    static final AtomicInteger stolenLocks = new AtomicInteger();

    static boolean writeDbWithToken(long token) {
        long cur;
        do {
            cur = dbMaxToken.get();
            if (token <= cur) return false; // Rejected stale lock holder!
        } while (!dbMaxToken.compareAndSet(cur, token));
        return true;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(2);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            lock.put("k", "A");
            long tokA = fencingSeq.incrementAndGet();
            long tokB = fencingSeq.incrementAndGet();
            Thread b = new Thread(() -> {
                try { barrier.await(); lock.put("k", "B"); writeDbWithToken(tokB); } catch (Exception e) { throw new RuntimeException(e); }
            });
            Thread a = new Thread(() -> {
                try {
                    barrier.await();
                    Thread.yield();
                    // Even if A wakes up late from GC, DB rejects tokA <= tokB, and Lua refuses to del B
                    writeDbWithToken(tokA);
                    lock.remove("k", "A");
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            a.start(); b.start(); a.join(); b.join();
            if (!"B".equals(lock.get("k"))) stolenLocks.incrementAndGet();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("stolenLocks=" + stolenLocks.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
