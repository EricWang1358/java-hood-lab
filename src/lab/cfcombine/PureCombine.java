package lab.cfcombine;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PureCombine {
    static final int WAVES = 400;
    static final int BATCH = 30;

    static int calcPart(int id, int kind) {
        int h = id * 131 + kind * 17;
        for (int i = 0; i < 16; i++) h = (h * 31) ^ (h >>> 7) + i;
        return (h & 63) + 10;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        ExecutorService pool = Executors.newWorkStealingPool(threads);
        int[] expect = new int[WAVES];
        for (int w = 0; w < WAVES; w++) {
            for (int i = 0; i < BATCH * threads; i++) {
                for (int k = 0; k < 3; k++) {
                    expect[w] += calcPart(w * 300 + i, k);
                }
            }
        }
        long start = System.nanoTime();
        int okTotals = 0;
        for (int w = 0; w < WAVES; w++) {
            @SuppressWarnings("unchecked")
            CompletableFuture<Integer>[] tasks =
                new CompletableFuture[BATCH * threads * 3];
            int idx = 0;
            for (int i = 0; i < BATCH * threads; i++) {
                int id = w * 300 + i;
                for (int k = 0; k < 3; k++) {
                    int kind = k;
                    tasks[idx++] =
                        CompletableFuture.supplyAsync(() -> calcPart(id, kind), pool);
                }
            }
            CompletableFuture.allOf(tasks).join();
            int sum = 0;
            for (CompletableFuture<Integer> f : tasks) sum += f.join();
            if (sum == expect[w]) okTotals++;
        }
        pool.shutdown();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("okTotals=" + okTotals);
        System.out.println("expected=" + WAVES);
        System.out.println("ms=" + ms);
    }
}
