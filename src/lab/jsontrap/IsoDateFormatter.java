package lab.jsontrap;

import java.math.BigDecimal;

public class IsoDateFormatter {
    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int exact = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            // 金额统一用整数「分」存储传输 + Long 转 String，彻底零浮点误差
            long cents = 10L + 20L;
            String idStr = Long.toString(1789234567890123457L + i);
            if (cents == 30L && Long.parseLong(idStr) == 1789234567890123457L + i) exact++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("exact=" + exact);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
