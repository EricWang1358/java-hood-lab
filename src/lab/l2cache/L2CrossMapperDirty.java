package lab.l2cache;

import java.util.HashMap;
import java.util.Map;

public class L2CrossMapperDirty {
    public static void main(String[] args) {
        int n = (args.length > 0 ? Integer.parseInt(args[0]) : 400);
        long t0 = System.nanoTime();
        int consistent = 0;
        for (int i = 0; i < n; i++) {
            String[] dbUserName = {"Alice"};
            Map<String, String> orderMapperL2 = new HashMap<>();
            Map<String, String> userMapperL2 = new HashMap<>();

            // OrderMapper 联表查询并写入 OrderMapper 自己的二级缓存
            orderMapperL2.computeIfAbsent("order:101", k -> "Order#101:" + dbUserName[0]);

            // UserMapper 更新用户名并清空 UserMapper 自己的二级缓存
            dbUserName[0] = "Bob";
            userMapperL2.clear(); // 注意：OrderMapper 的二级缓存完全不知道！

            // 再次通过 OrderMapper 联表查询，命中旧的二级缓存 "Alice"
            String read = orderMapperL2.computeIfAbsent("order:101", k -> "Order#101:" + dbUserName[0]);
            if (read.endsWith("Bob")) {
                consistent++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"consistent\":%d,\"expected\":%d,\"ms\":%d}%n", consistent, n, ms);
    }
}
