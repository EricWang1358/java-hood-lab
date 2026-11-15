package lab.sqlinject;

public class LikeConcatTrap {
    public static void main(String[] args) {
        int n = (args.length > 0 ? Integer.parseInt(args[0]) : 4) * 1000;
        long t0 = System.nanoTime();
        int safe = 0;
        for (int i = 0; i < n; i++) {
            String kw = (i % 7 == 0) ? "%' UNION SELECT password FROM admin --" : "book_" + i;
            // 常见误区：以为 LIKE '%#{kw}%' 会报错就改成 LIKE '%${kw}%'
            String sql = "SELECT id, title FROM goods WHERE title LIKE '%" + kw + "%'";
            if (!sql.contains("UNION SELECT")) {
                safe++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"safe_queries\":%d,\"expected\":%d,\"ms\":%d}%n", safe, n, ms);
    }
}
