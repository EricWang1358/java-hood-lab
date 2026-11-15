package lab.lhmlru;

import java.util.HashMap;
import java.util.Map;

public class HashMapNoEvictLeak {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int cap = 100;
        Map<Integer, Integer> cache = new HashMap<>();
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) cache.put(i, i);
        long ms = (System.nanoTime() - start) / 1_000_000;
        int ok = cache.size() <= cap ? 1 : 0;
        System.out.println("boundedOk=" + ok);
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
