package lab.l2cache;

import java.util.HashMap;
import java.util.Map;

public class L2UncommittedLeak {
    public static void main(String[] args) {
        int n = (args.length > 0 ? Integer.parseInt(args[0]) : 400);
        long t0 = System.nanoTime();
        int consistent = 0;
        for (int i = 0; i < n; i++) {
            String[] dbUserName = {"Alice"};
            Map<String, String> orderMapperL2 = new HashMap<>();

            // 跨服务或原生 SQL 直接改库（绕过 Mapper），二级缓存完全无法感知
            orderMapperL2.put("order:101", "Order#101:" + dbUserName[0]);
            dbUserName[0] = "Bob";

            String read = orderMapperL2.getOrDefault("order:101", "Order#101:" + dbUserName[0]);
            if (read.endsWith("Bob")) {
                consistent++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"consistent\":%d,\"expected\":%d,\"ms\":%d}%n", consistent, n, ms);
    }
}
