package lab.generics;

import java.util.ArrayList;
import java.util.List;

public class WildcardPecsSafe {
    static double sumNumbers(List<? extends Number> src) {
        double s = 0;
        for (Number num : src) s += num.doubleValue();
        return s;
    }

    static void addIntegers(List<? super Integer> dst) {
        dst.add(10);
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int safeOk = 0;
        for (int i = 0; i < n; i++) {
            List<Integer> list = new ArrayList<>();
            addIntegers(list);
            if (sumNumbers(list) == 10.0) safeOk++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("safeOk=" + safeOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
