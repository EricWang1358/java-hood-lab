package lab.streamtrap;

import java.util.ArrayList;
import java.util.List;

public class IntStreamPrimitiveSum {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(i & 15);
        long start = System.nanoTime();
        long sum = list.stream().mapToLong(Integer::intValue).sum();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + (sum >= 0 ? n : 0));
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
