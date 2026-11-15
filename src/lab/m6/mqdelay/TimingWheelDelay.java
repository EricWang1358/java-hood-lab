package lab.m6.mqdelay;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TimingWheelDelay {
    static final int ORDERS = 200;
    static final int SLOTS = 32;
    @SuppressWarnings("unchecked")
    static final ArrayList<Integer>[] wheel = new ArrayList[SLOTS];
    static final AtomicInteger scanOps = new AtomicInteger();

    static {
        for (int i = 0; i < SLOTS; i++) wheel[i] = new ArrayList<>();
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        for (int i = 0; i < ORDERS; i++) {
            int due = (10 + (i % 15)) % SLOTS;
            wheel[due].add(i); // O(1) insert into timing wheel slot
            scanOps.incrementAndGet();
        }
        for (int tick = 0; tick < SLOTS; tick++) {
            for (Integer id : wheel[tick]) scanOps.incrementAndGet(); // O(1) per fired task
            wheel[tick].clear();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanOps=" + scanOps.get());
        System.out.println("expected=" + (ORDERS * 2));
        System.out.println("ms=" + ms);
    }
}
