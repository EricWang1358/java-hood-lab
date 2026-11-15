package lab.cfcombine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Plain {
    static final int WAVES = 400;
    static final int BATCH = 30;
    static final String[] KINDS = {"coupon", "points", "ship"};

    static int calcPart(int id, int kind) {
        int h = id * 131 + kind * 17;
        for (int i = 0; i < 16; i++) h = (h * 31) ^ (h >>> 7) + i;
        return (h & 63) + 10;
    }

    static void record(Map<String, Integer> bill, String kind, int val) {
        int cur = bill.getOrDefault("total", 0);
        bill.put("total", cur + val);
        int sub = bill.getOrDefault(kind, 0);
        bill.put(kind, sub + val);
        bill.put("count", bill.getOrDefault("count", 0) + 1);
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        ExecutorService pool = Executors.newWorkStealingPool(threads);
        @SuppressWarnings("unchecked")
        Map<String, Integer>[] bills = new HashMap[WAVES];
        int[] expect = new int[WAVES];
        for (int w = 0; w < WAVES; w++) {
            bills[w] = new HashMap<>(8);
            for (int i = 0; i < BATCH * threads; i++) {
                for (int k = 0; k < 3; k++) {
                    expect[w] += calcPart(w * 300 + i, k);
                }
            }
        }
        long start = System.nanoTime();
        for (int w = 0; w < WAVES; w++) {
            Map<String, Integer> bill = bills[w];
            for (int i = 0; i < BATCH * threads; i++) {
                int id = w * 300 + i;
                for (int k = 0; k < 3; k++) {
                    int kind = k;
                    CompletableFuture.runAsync(
                        () -> record(bill, KINDS[kind], calcPart(id, kind)), pool);
                }
            }
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        long ms = (System.nanoTime() - start) / 1_000_000;
        int okTotals = 0;
        for (int w = 0; w < WAVES; w++) {
            if (Integer.valueOf(expect[w]).equals(bills[w].get("total"))) okTotals++;
        }
        System.out.println("okTotals=" + okTotals);
        System.out.println("expected=" + WAVES);
        System.out.println("ms=" + ms);
    }
}
