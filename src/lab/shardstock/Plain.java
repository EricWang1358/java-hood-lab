package lab.shardstock;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

public class Plain {
    static final int WAVES_PER_THREAD = 60;
    static final int STOCK = 800;
    static int stock = STOCK;
    static final AtomicLong sold = new AtomicLong();
    static volatile long sink;

    static long verify(int reqId) {
        long h = reqId * 31L + 7;
        for (int i = 0; i < 240; i++) h = (h * 31 + i) ^ (h >>> 5);
        return h | 1L;
    }

    static long buyOne(int reqId) {
        if (stock > 0) {
            stock--;
            return verify(reqId);
        }
        return 0L;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int waves = WAVES_PER_THREAD * threads;
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> stock = STOCK);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            int startReq = t;
            workers[t] = new Thread(() -> {
                try {
                    long mine = 0, sum = 0;
                    int req = startReq;
                    for (int w = 0; w < waves; w++) {
                        barrier.await();
                        long code;
                        while ((code = buyOne(req++)) != 0L) {
                            mine++;
                            sum += code;
                        }
                    }
                    sold.addAndGet(mine);
                    sink = sum;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("sold=" + sold.get());
        System.out.println("expected=" + ((long) waves * STOCK));
        System.out.println("ms=" + ms);
    }
}
