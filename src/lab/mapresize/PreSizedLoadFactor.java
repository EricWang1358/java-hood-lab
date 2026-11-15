package lab.mapresize;

import java.util.HashMap;
import java.util.Map;

public class PreSizedLoadFactor {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int cap = (int) (n / 0.75f) + 1;
        Map<Integer, Integer> map = new HashMap<>(cap);
        for (int i = 0; i < n; i++) map.put(i, i);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("size=" + map.size());
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
