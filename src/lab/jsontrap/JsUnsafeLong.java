package lab.jsontrap;

public class JsUnsafeLong {
    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int exact = 0;
        long snowflakeId = 1789234567890123457L; // 19 位雪花 ID，超过 JS Number.MAX_SAFE_INTEGER (2^53-1)
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            long id = snowflakeId + i;
            double jsNumber = (double) id; // 前端 JSON.parse 按 IEEE-754 double 解析
            long roundTrip = (long) jsNumber;
            if (roundTrip == id) exact++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("exact=" + exact);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
