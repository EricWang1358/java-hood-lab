package lab.m6.ratelimit;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class SlidingWindowLog {
    static final int LIMIT = 10;
    static final int WINDOW_TICKS = 10;
    static final int TICKS = 100;
    static final AtomicInteger overloadBreaches = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        ArrayDeque<Integer> log = new ArrayDeque<>();
        int[] admittedAtTick = new int[TICKS];
        long start = System.nanoTime();

        for (int t = 0; t < TICKS; t++) {
            while (!log.isEmpty() && log.peekFirst() <= t - WINDOW_TICKS) {
                log.pollFirst();
            }
            int burst = 12 * threads;
            for (int r = 0; r < burst; r++) {
                if (log.size() < LIMIT) {
                    log.addLast(t);
                    admittedAtTick[t]++;
                }
            }
        }
        for (int t = 0; t < TICKS - 1; t++) {
            int sum = admittedAtTick[t] + admittedAtTick[t + 1];
            if (sum > LIMIT) overloadBreaches.addAndGet(sum - LIMIT);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("overloadBreaches=" + overloadBreaches.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
