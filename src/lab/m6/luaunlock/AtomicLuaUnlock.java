package lab.m6.luaunlock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicLuaUnlock {
    static final int ROUNDS = 40;
    static final ConcurrentHashMap<String, String> lock = new ConcurrentHashMap<>();
    static final AtomicInteger stolenLocks = new AtomicInteger();

    static void spin(int n) {
        long x = n;
        for (int i = 0; i < n; i++) x = x * 31 + i;
        if (x == 42) System.out.print("");
    }

    static boolean luaUnlock(String owner) {
        // Atomic single-thread Lua: if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) end
        return lock.remove("k", owner);
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(2);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            lock.put("k", "A");
            Thread a = new Thread(() -> {
                try { barrier.await(); spin(15_000); luaUnlock("A"); } catch (Exception e) { throw new RuntimeException(e); }
            });
            Thread b = new Thread(() -> {
                try { barrier.await(); spin(3_000); lock.put("k", "B"); } catch (Exception e) { throw new RuntimeException(e); }
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
