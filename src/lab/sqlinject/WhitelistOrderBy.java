package lab.sqlinject;

import java.util.Set;

public class WhitelistOrderBy {
    private static final Set<String> ALLOWED_COLS = Set.of("id", "create_time", "price");

    public static void main(String[] args) {
        int n = (args.length > 0 ? Integer.parseInt(args[0]) : 4) * 1000;
        long t0 = System.nanoTime();
        int safe = 0;
        for (int i = 0; i < n; i++) {
            String sortCol = (i % 7 == 0) ? "id; DROP TABLE users--" : "price";
            // ORDER BY 列名不能用 #{}（会变成字符串常量），必须用白名单校验后再用 ${}
            String safeCol = ALLOWED_COLS.contains(sortCol) ? sortCol : "id";
            String sql = "SELECT id, price FROM goods ORDER BY " + safeCol + " DESC";
            if (!sql.contains("DROP TABLE")) {
                safe++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"safe_queries\":%d,\"expected\":%d,\"ms\":%d}%n", safe, n, ms);
    }
}
