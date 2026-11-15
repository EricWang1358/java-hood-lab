package lab.jsontrap;

import java.math.BigDecimal;

public class LongAsStringBigDecimal {
    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int exact = 0;
        long snowflakeId = 1789234567890123457L;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            // 1. Long 序列化为 String 传给前端，再按 String 解析回 Long
            String jsonStr = Long.toString(snowflakeId + i);
            long roundTrip = Long.parseLong(jsonStr);
            // 2. 金额用 BigDecimal("0.1") 字符串构造
            BigDecimal sum = new BigDecimal("0.1").add(new BigDecimal("0.2"));
            if (roundTrip == snowflakeId + i && sum.compareTo(new BigDecimal("0.3")) == 0) exact++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("exact=" + exact);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
