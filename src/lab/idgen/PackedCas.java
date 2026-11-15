package lab.idgen;

import java.util.BitSet;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PackedCas {
    static final int WAVES = 55;
    static final int BATCH = 4_000;
    static final int SEQ_LIMIT = 8;
    static final long SEQ_MASK = (1L << 20) - 1;

    static final AtomicLong packed = new AtomicLong(0);
    static volatile int sink;

    static long nextId() {
        while (true) {
            long cur = packed.get();
            long w = cur >>> 20;
            int s = (int) (cur & SEQ_MASK);
            long next = (s + 1 >= SEQ_LIMIT)
                ? ((w + 1) << 20)
                : ((w << 20) | (s + 1));
            if (packed.compareAndSet(cur, next)) {
                return cur;
            }
        }
    }

    static int encode(int pace, long id) {
        for (int k = 0; k < 20; k++) pace = pace * 31 + (int) id + k;
        return pace;
    }

    static int countUnique(long[][] ids, BitSet seen) {
        seen.clear();
        int unique = 0;
        for (long[] row : ids) {
            for (long id : row) {
                int bit = (int) (((id >>> 20) << 5) | (id & SEQ_MASK));
                if (!seen.get(bit)) {
                    seen.set(bit);
                    unique++;
                }
            }
        }
        return unique;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        long[][] ids = new long[threads][BATCH];
        BitSet seen = new BitSet();
        AtomicInteger uniqueIds = new AtomicInteger();
        AtomicInteger wave = new AtomicInteger();
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> {
            if (wave.getAndIncrement() > 0) {
                uniqueIds.addAndGet(countUnique(ids, seen));
                packed.set(0);
            }
        });
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int tid = t;
            workers[t] = new Thread(() -> {
                try {
                    int pace = tid + 1;
                    long[] mine = ids[tid];
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        for (int i = 0; i < BATCH; i++) {
                            mine[i] = nextId();
                            pace = encode(pace, mine[i]);
                        }
                    }
                    sink = pace;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        uniqueIds.addAndGet(countUnique(ids, seen));
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("uniqueIds=" + uniqueIds.get());
        System.out.println("expected=" + (WAVES * threads * BATCH));
        System.out.println("ms=" + ms);
    }
}
