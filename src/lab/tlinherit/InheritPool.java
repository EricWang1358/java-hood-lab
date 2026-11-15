package lab.tlinherit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class InheritPool {
    static final int REQUESTS = 100;
    static final InheritableThreadLocal<String> TRACE = new InheritableThreadLocal<>();
    static volatile int sink;

    static String handleSubTask() {
        int x = 1;
        for (int i = 0; i < 6_000; i++) x = x * 31 + i;
        sink = x;
        return TRACE.get();
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        AtomicInteger okTraces = new AtomicInteger();
        long start = System.nanoTime();
        for (int r = 0; r < REQUESTS; r++) {
            String traceId = "req-" + r;
            TRACE.set(traceId);
            try {
                List<Future<String>> futures = new ArrayList<>(threads);
                for (int i = 0; i < threads; i++) {
                    futures.add(pool.submit(() -> handleSubTask()));
                }
                for (Future<String> f : futures) {
                    if (traceId.equals(f.get())) okTraces.incrementAndGet();
                }
            } finally {
                TRACE.remove();
            }
        }
        pool.shutdown();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("okTraces=" + okTraces.get());
        System.out.println("expected=" + (REQUESTS * threads));
        System.out.println("ms=" + ms);
    }
}
