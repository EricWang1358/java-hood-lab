package lab.generics;

import java.util.ArrayList;
import java.util.List;

public class RawTypeHeapPollution {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int safeOk = 0;
        for (int i = 0; i < n; i++) {
            List<Integer> nums = new ArrayList<>();
            List raw = nums;
            raw.add("bad-string");
            try {
                Integer v = nums.get(0);
                if (v != null) safeOk++;
            } catch (ClassCastException ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("safeOk=" + safeOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
