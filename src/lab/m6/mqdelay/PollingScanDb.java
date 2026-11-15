package lab.m6.mqdelay;

import java.util.concurrent.atomic.AtomicInteger;

public class PollingScanDb {
    static final int ORDERS = 200;
    static final int TICKS = 30;
    static final int[] dueTick = new int[ORDERS];
    static final AtomicInteger scanOps = new AtomicInteger();

    static {
        for (int i = 0; i < ORDERS; i++) dueTick[i] = 10 + (i % 15);
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        for (int tick = 0; tick < TICKS; tick++) {
            for (int t = 0; t < threads; t++) {
                for (int i = 0; i < ORDERS; i++) {
                    scanOps.incrementAndGet(); // full table scan every tick!
                }
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanOps=" + scanOps.get());
        System.out.println("expected=" + (ORDERS * 2));
        System.out.println("ms=" + ms);
    }
}
