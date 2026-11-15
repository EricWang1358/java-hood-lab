package lab.l1cache;

import java.util.HashMap;
import java.util.Map;

public class SessionL1Stale {
    public static void main(String[] args) {
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 400;
        long t0 = System.nanoTime();
        int fresh = 0;
        for (int i = 0; i < n; i++) {
            int[] dbBalance = {1000};
            Map<String, Integer> l1Cache = new HashMap<>();

            // 同一个 SqlSession / @Transactional 内第一次查询，结果写入一级缓存
            int first = l1Cache.computeIfAbsent("user:1", k -> dbBalance[0]);
            // 外部事务或原生 JDBC 把数据库余额扣减为 800
            dbBalance[0] = 800;
            // 同一个 SqlSession 内第二次查询，直接命中一级缓存返回旧值 1000！
            int second = l1Cache.computeIfAbsent("user:1", k -> dbBalance[0]);
            if (first == 1000 && second == 800) {
                fresh++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"fresh_reads\":%d,\"expected\":%d,\"ms\":%d}%n", fresh, n, ms);
    }
}
