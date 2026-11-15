package lab.failfast;

import java.util.ArrayList;
import java.util.List;

public class RemoveIfBatchFast {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(i);
        long start = System.nanoTime();
        list.removeIf(x -> (x & 1) == 0);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + list.size());
        System.out.println("expected=" + (n / 2));
        System.out.println("ms=" + ms);
    }
}
