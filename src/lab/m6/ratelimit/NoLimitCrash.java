package lab.m6.ratelimit;

import java.util.concurrent.atomic.AtomicInteger;

public class NoLimitCrash {
    static final int LIMIT_PER_WINDOW = 10;
    static final int WINDOWS = 20;
    static final AtomicInteger overloadBreaches = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Simulates burst traffic arriving at boundary ticks with zero rate limiting
        for (int w = 0; w < WINDOWS; w++) {
            int incoming = 12 * threads;
            if (incoming > LIMIT_PER_WINDOW) {
                overloadBreaches.addAndGet(incoming - LIMIT_PER_WINDOW);
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("overloadBreaches=" + overloadBreaches.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
