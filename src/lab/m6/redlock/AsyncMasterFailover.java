package lab.m6.redlock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncMasterFailover {
    static final int ROUNDS = 30;
    static final ConcurrentHashMap<String, String> master = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, String> replica = new ConcurrentHashMap<>();
    static final AtomicInteger conflicts = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        CyclicBarrier barrier = new CyclicBarrier(2);
        long start = System.nanoTime();
        for (int r = 0; r < ROUNDS; r++) {
            master.clear();
            replica.clear();
            Thread t1 = new Thread(() -> {
                try {
                    barrier.await();
                    master.putIfAbsent("lock:sku", "client-A");
                    // Master crashes BEFORE async replication reaches replica!
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            Thread t2 = new Thread(() -> {
                try {
                    barrier.await();
                    Thread.yield();
                    // Replica promoted to new Master, has no lock -> client-B also acquires!
                    if (replica.putIfAbsent("lock:sku", "client-B") == null && master.containsKey("lock:sku")) {
                        conflicts.incrementAndGet();
                    }
                } catch (Exception e) { throw new RuntimeException(e); }
            });
            t1.start(); t2.start(); t1.join(); t2.join();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("conflicts=" + conflicts.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
