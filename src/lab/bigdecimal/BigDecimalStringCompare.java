package lab.bigdecimal;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalStringCompare {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int exactMatch = 0;
        BigDecimal target = new BigDecimal("0.30");
        for (int i = 0; i < n; i++) {
            BigDecimal sum = new BigDecimal("0.1").add(new BigDecimal("0.2"))
                    .setScale(2, RoundingMode.HALF_UP);
            if (sum.compareTo(target) == 0) exactMatch++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("exactMatch=" + exactMatch);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
