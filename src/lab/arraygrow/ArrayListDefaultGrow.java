package lab.arraygrow;

import java.util.ArrayList;
import java.util.List;

public class ArrayListDefaultGrow {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) list.add(i);
        long sum = 0;
        for (int i = 0; i < n; i += 4) sum += list.get(i);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("count=" + (sum > 0 ? list.size() : 0));
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
