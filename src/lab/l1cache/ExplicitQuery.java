package lab.l1cache;

import java.util.HashMap;
import java.util.Map;

public class ExplicitQuery {
    public static void main(String[] args) {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 400;
        long t0 = System.nanoTime();
        int fresh = 0;
        for (int i = 0; i < n; i++) {
            int[] dbBalance = {1000};
            Map<String, Integer> l1Cache = new HashMap<>();
            int first = l1Cache.computeIfAbsent("user:1", k -> dbBalance[0]);

            // 同 SqlSession 内执行任何 insert/update/delete 或显式 clearCache() 都会清空一级缓存
            dbBalance[0] = 800;
            l1Cache.clear();
            int second = l1Cache.computeIfAbsent("user:1", k -> dbBalance[0]);
            if (first == 1000 && second == 800) {
                fresh++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"fresh_reads\":%d,\"expected\":%d,\"ms\":%d}%n", fresh, n, ms);
    }
}
