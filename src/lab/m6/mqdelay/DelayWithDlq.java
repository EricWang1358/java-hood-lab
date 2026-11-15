package lab.m6.mqdelay;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DelayWithDlq {
    static final int ORDERS = 200;
    static final int SLOTS = 32;
    @SuppressWarnings("unchecked")
    static final ArrayList<Integer>[] ttlQueue = new ArrayList[SLOTS];
    static final ArrayList<Integer> dlq = new ArrayList<>();
    static final AtomicInteger scanOps = new AtomicInteger();

    static {
        for (int i = 0; i < SLOTS; i++) ttlQueue[i] = new ArrayList<>();
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        for (int i = 0; i < ORDERS; i++) {
            ttlQueue[(10 + (i % 15)) % SLOTS].add(i);
            scanOps.incrementAndGet();
        }
        for (int tick = 0; tick < SLOTS; tick++) {
            dlq.addAll(ttlQueue[tick]);
            ttlQueue[tick].clear();
        }
        for (Integer expiredId : dlq) {
            scanOps.incrementAndGet(); // consumer closes unpaid order from DLQ
        }
        dlq.clear();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanOps=" + scanOps.get());
        System.out.println("expected=" + (ORDERS * 2));
        System.out.println("ms=" + ms);
    }
}
