package lab.generics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CheckedListRuntimeGuard {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int safeOk = 0;
        for (int i = 0; i < n; i++) {
            List<Integer> nums = Collections.checkedList(new ArrayList<>(), Integer.class);
            List raw = nums;
            try {
                raw.add("bad-string");
            } catch (ClassCastException blocked) {
                safeOk++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("safeOk=" + safeOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
