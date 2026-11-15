package lab.l2cache;

import java.util.HashMap;
import java.util.Map;

public class CacheRefShared {
    public static void main(String[] args) {
        int n = (args.length > 0 ? Integer.parseInt(args[0]) : 400);
        long t0 = System.nanoTime();
        int consistent = 0;
        for (int i = 0; i < n; i++) {
            String[] dbUserName = {"Alice"};
            // <cache-ref namespace="UserMapper"/> 让两个 Mapper 共享同一块二级缓存命名空间
            Map<String, String> sharedL2 = new HashMap<>();

            sharedL2.computeIfAbsent("order:101", k -> "Order#101:" + dbUserName[0]);

            // UserMapper 更新时清空共享命名空间，OrderMapper 缓存同步失效
            dbUserName[0] = "Bob";
            sharedL2.clear();

            String read = sharedL2.computeIfAbsent("order:101", k -> "Order#101:" + dbUserName[0]);
            if (read.endsWith("Bob")) {
                consistent++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"consistent\":%d,\"expected\":%d,\"ms\":%d}%n", consistent, n, ms);
    }
}
