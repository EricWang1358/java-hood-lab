package lab.tlinherit;

import java.util.concurrent.atomic.AtomicInteger;

public class NewThreadEach {
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
        AtomicInteger okTraces = new AtomicInteger();
        long start = System.nanoTime();
        for (int r = 0; r < REQUESTS; r++) {
            String traceId = "req-" + r;
            TRACE.set(traceId);
            try {
                Thread[] workers = new Thread[threads];
                String[] got = new String[threads];
                for (int i = 0; i < threads; i++) {
                    int idx = i;
                    workers[i] = new Thread(() -> got[idx] = handleSubTask());
                    workers[i].start();
                }
                for (int i = 0; i < threads; i++) {
                    workers[i].join();
                    if (traceId.equals(got[i])) okTraces.incrementAndGet();
                }
            } finally {
                TRACE.remove();
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("okTraces=" + okTraces.get());
        System.out.println("expected=" + (REQUESTS * threads));
        System.out.println("ms=" + ms);
    }
}
