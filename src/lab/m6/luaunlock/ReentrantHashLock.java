package lab.m6.luaunlock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class ReentrantHashLock {
    static final int ROUNDS = 40;
    static final ConcurrentHashMap<String, Integer> hashLock = new ConcurrentHashMap<>();
    static final AtomicInteger stolenLocks = new AtomicInteger();

    static synchronized boolean hincrLock(String field) {
        if (hashLock.isEmpty()) {
            hashLock.put(field, 1);
            return true;
        }
        Integer cnt = hashLock.get(field);
        if (cnt != null) {
            hashLock.put(field, cnt + 1);
            return true;
        }
        return false;
    }

    static synchronized void hincrUnlock(String field) {
        Integer cnt = hashLock.get(field);
        if (cnt == null) return;
        if (cnt <= 1) hashLock.remove(field);
        else hashLock.put(field, cnt - 1);
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(2);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            hashLock.clear();
            hincrLock("A");
            hincrLock("A"); // reentrant depth = 2
            Thread a = new Thread(() -> {
                try {
                    barrier.await();
                    hincrUnlock("A"); // depth 2 -> 1, still held by A!
                    hincrUnlock("A"); // depth 1 -> 0, released
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            Thread b = new Thread(() -> {
                try {
                    barrier.await();
                    hincrUnlock("B"); // wrong owner cannot touch A
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            a.start(); b.start(); a.join(); b.join();
            if (!hashLock.isEmpty()) stolenLocks.incrementAndGet();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("stolenLocks=" + stolenLocks.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
