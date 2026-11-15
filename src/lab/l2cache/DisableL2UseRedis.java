package lab.l2cache;

import java.util.HashMap;
import java.util.Map;

public class DisableL2UseRedis {
    public static void main(String[] args) {
        int n = (args.length > 0 ? Integer.parseInt(args[0]) : 400);
        long t0 = System.nanoTime();
        int consistent = 0;
        for (int i = 0; i < n; i++) {
            String[] dbUserName = {"Alice"};
            Map<String, String> redisCache = new HashMap<>();

            // 关闭 MyBatis 二级缓存，改用业务可控的 Redis Cache-Aside
            redisCache.computeIfAbsent("order:101", k -> "Order#101:" + dbUserName[0]);

            // 更新用户时精准删除关联订单缓存 Key
            dbUserName[0] = "Bob";
            redisCache.remove("order:101");

            String read = redisCache.computeIfAbsent("order:101", k -> "Order#101:" + dbUserName[0]);
            if (read.endsWith("Bob")) {
                consistent++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"consistent\":%d,\"expected\":%d,\"ms\":%d}%n", consistent, n, ms);
    }
}
