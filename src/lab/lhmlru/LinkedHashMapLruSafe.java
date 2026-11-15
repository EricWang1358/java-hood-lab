package lab.lhmlru;

import java.util.LinkedHashMap;
import java.util.Map;

public class LinkedHashMapLruSafe {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int cap = 100;
        Map<Integer, Integer> cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > cap;
            }
        };
        long start = System.nanoTime();
        for (int i = 0; i < cap; i++) cache.put(i, i);
        for (int i = 0; i < n; i++) cache.get(0);
        cache.put(999999, 1);
        long ms = (System.nanoTime() - start) / 1_000_000;
        int ok = (cache.size() <= cap && cache.containsKey(0)) ? 1 : 0;
        System.out.println("boundedOk=" + ok);
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
