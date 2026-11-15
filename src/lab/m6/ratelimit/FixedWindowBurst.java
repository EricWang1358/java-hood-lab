package lab.m6.ratelimit;

import java.util.concurrent.atomic.AtomicInteger;

public class FixedWindowBurst {
    static final int LIMIT = 10;
    static final int TICKS = 100;
    static final AtomicInteger overloadBreaches = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int[] windowCount = new int[10];
        int[] admittedAtTick = new int[TICKS];
        long start = System.nanoTime();

        // Burst hits the end of window k (tick k*10+9) and start of window k+1 (tick (k+1)*10)
        for (int w = 0; w < 10; w++) {
            int endTick = w * 10 + 9;
            int nextTick = w * 10 + 10;
            for (int r = 0; r < LIMIT; r++) {
                if (windowCount[w] < LIMIT) {
                    windowCount[w]++;
                    admittedAtTick[endTick]++;
                }
            }
            if (w + 1 < 10) {
                for (int r = 0; r < LIMIT; r++) {
                    if (windowCount[w + 1] < LIMIT) {
                        windowCount[w + 1]++;
                        admittedAtTick[nextTick]++;
                    }
                }
            }
        }
        // Any 2 consecutive ticks span across boundary -> 2*LIMIT=20 requests in 20ms (> LIMIT=10)!
        for (int t = 0; t < TICKS - 1; t++) {
            int twoTickSum = admittedAtTick[t] + admittedAtTick[t + 1];
            if (twoTickSum > LIMIT) {
                overloadBreaches.addAndGet(twoTickSum - LIMIT);
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("overloadBreaches=" + overloadBreaches.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
