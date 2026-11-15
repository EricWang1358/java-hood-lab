package lab.tslru;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UnsafeAccessLru {
    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int ops = 100_000;
        Map<Integer, Integer> lru = new LinkedHashMap<>(128, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > 64;
            }
        };
        for (int i = 0; i < 64; i++) lru.put(i, i);
        AtomicInteger errors = new AtomicInteger();
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                for (int i = 0; i < ops; i++) {
                    try {
                        if ((i & 7) == 0) lru.put(i & 127, i);
                        else lru.get(i & 127);
                    } catch (Throwable ex) {
                        errors.incrementAndGet();
                    }
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        int ok = (errors.get() == 0 && lru.size() <= 64) ? 1 : 0;
        System.out.println("ok=" + ok);
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
