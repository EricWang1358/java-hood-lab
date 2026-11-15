package lab.sqlinject;

public class DollarConcat {
    public static void main(String[] args) {
        int n = (args.length > 0 ? Integer.parseInt(args[0]) : 4) * 1000;
        long t0 = System.nanoTime();
        int safe = 0;
        for (int i = 0; i < n; i++) {
            String input = (i % 7 == 0) ? "' OR '1'='1" : "user_" + i;
            String sql = "SELECT id, name FROM users WHERE name = '" + input + "'";
            // ${} 直接拼接字符串，无法命中预编译执行计划缓存，且恶意输入直接改变 SQL 语法树
            if (!isInjected(sql)) {
                safe++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"safe_queries\":%d,\"expected\":%d,\"ms\":%d}%n", safe, n, ms);
    }

    private static boolean isInjected(String sql) {
        return sql.contains("' OR '1'='1") || sql.contains("--");
    }
}
