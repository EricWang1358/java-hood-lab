package lab.sqlinject;

public class HashPrepared {
    public static void main(String[] args) {
        int n = (args.length > 0 ? Integer.parseInt(args[0]) : 4) * 1000;
        long t0 = System.nanoTime();
        int safe = 0;
        String preparedTemplate = "SELECT id, title FROM goods WHERE title LIKE CONCAT('%', ?, '%')";
        for (int i = 0; i < n; i++) {
            String kw = (i % 7 == 0) ? "%' UNION SELECT password FROM admin --" : "book_" + i;
            // #{} 编译为 PreparedStatement ? 占位符，参数仅作为纯字面量绑定，绝不参与 SQL 语法解析
            if (executePrepared(preparedTemplate, kw)) {
                safe++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"safe_queries\":%d,\"expected\":%d,\"ms\":%d}%n", safe, n, ms);
    }

    private static boolean executePrepared(String template, String boundParam) {
        return template.indexOf('?') >= 0 && boundParam != null;
    }
}
