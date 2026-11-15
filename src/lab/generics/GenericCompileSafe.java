package lab.generics;

import java.util.ArrayList;
import java.util.List;

public class GenericCompileSafe {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int safeOk = 0;
        for (int i = 0; i < n; i++) {
            List<Integer> nums = new ArrayList<>();
            nums.add(42);
            Integer v = nums.get(0);
            if (v == 42) safeOk++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("safeOk=" + safeOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
