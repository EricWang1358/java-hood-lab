package lab.txoutbox;

import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;

public class GlobalDualLock {
    static final int KEYS = 32;
    static final int OPS_PER_WAVE = 256;
    static final int WAVES = 16;
    static final int FAST = 100000;
    static final int SLOW = 1000000;

    static final Object LOCK = new Object();
    static final int[] db = new int[KEYS];
    static final int[] proj = new int[KEYS];
    static volatile long sink;

    static int work(int n) {
        long x = 0x9e3779b97f4a7c15L ^ n;
        for (int i = 0; i < n; i++) {
            x = x * 6364136223846793005L + 1442695040888963407L;
        }
        return (int) (x ^ (x >>> 32));
    }

    static int update(int k, int val, int cost) {
        synchronized (LOCK) {
            db[k] = val;
            int h = work(cost);
            proj[k] = val;
            return h;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int perThread = OPS_PER_WAVE / threads;
        int[] consistent = {0};
        CyclicBarrier start = new CyclicBarrier(threads);
        CyclicBarrier end = new CyclicBarrier(threads, () -> {
            for (int k = 0; k < KEYS; k++) {
                if (db[k] == proj[k]) consistent[0]++;
            }
            Arrays.fill(db, 0);
            Arrays.fill(proj, 0);
        });

        Thread[] ws = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            final int step = 1 + 2 * (t & 3);
            ws[t] = new Thread(() -> {
                long acc = 0;
                try {
                    start.await();
                    for (int w = 0; w < WAVES; w++) {
                        for (int i = 0; i < perThread; i++) {
                            int k = (i * step) % KEYS;
                            int val = ((tid + 1) << 16) | ((w + 1) << 8) | (i + 1);
                            int cost = (((i + tid) & 1) == 0) ? SLOW : FAST;
                            acc ^= update(k, val, cost);
                        }
                        end.await();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                sink = acc;
            });
        }

        long t0 = System.nanoTime();
        for (Thread w : ws) w.start();
        for (Thread w : ws) w.join();
        long ms = Math.max(1L, (System.nanoTime() - t0) / 1_000_000L);

        System.out.println("consistentKeys=" + consistent[0]);
        System.out.println("expected=" + (WAVES * KEYS));
        System.out.println("ms=" + ms);
    }
}
