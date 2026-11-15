package lab.userlock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class Plain {
    static final int WAVES = 25;
    static final int USERS = 160;
    static ConcurrentHashMap<String, Boolean> claimed = new ConcurrentHashMap<>(512);
    static final AtomicInteger claimedUsers = new AtomicInteger();
    static volatile long sink;

    static long issueCoupon(int uid) {
        claimedUsers.incrementAndGet();
        long x = uid * 31L + 7;
        for (int i = 0; i < 45_000; i++) x = x * 31 + i;
        return x;
    }

    static long tryClaim(int uid) {
        String key = new StringBuilder("u-").append(uid).toString();
        if (!claimed.containsKey(key)) {
            long code = issueCoupon(uid);
            claimed.put(key, Boolean.TRUE);
            return code;
        }
        return 0L;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> claimed = new ConcurrentHashMap<>(512));
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try {
                    long sum = 0;
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        for (int u = 0; u < USERS; u++) sum += tryClaim(u);
                    }
                    sink = sum;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("claimedUsers=" + claimedUsers.get());
        System.out.println("expected=" + (WAVES * USERS));
        System.out.println("ms=" + ms);
    }
}
