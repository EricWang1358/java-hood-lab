package lab.m6.ratelimit;

import java.util.concurrent.atomic.AtomicInteger;

public class TokenBucketSmooth {
    static final int CAPACITY = 10;
    static final int REFILL_PER_TICK = 1;
    static final int TICKS = 100;
    static final AtomicInteger overloadBreaches = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int tokens = CAPACITY;
        int[] admittedAtTick = new int[TICKS];
        long start = System.nanoTime();

        for (int t = 0; t < TICKS; t++) {
            tokens = Math.min(CAPACITY, tokens + REFILL_PER_TICK);
            int burst = 12 * threads;
            for (int r = 0; r < burst; r++) {
                if (tokens > 0) {
                    tokens--;
                    admittedAtTick[t]++;
                }
            }
        }
        for (int t = 0; t < TICKS - 1; t++) {
            int sum = admittedAtTick[t] + admittedAtTick[t + 1];
            if (sum > CAPACITY + REFILL_PER_TICK) {
                overloadBreaches.addAndGet(sum - (CAPACITY + REFILL_PER_TICK));
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("overloadBreaches=" + overloadBreaches.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
