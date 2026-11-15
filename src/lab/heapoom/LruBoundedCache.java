package lab.heapoom;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruBoundedCache {
    static final int MAX_ENTRIES = 64;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Map<Integer, byte[]> lru = new LinkedHashMap<>(128, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, byte[]> eldest) {
                return size() > MAX_ENTRIES;
            }
        };
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            lru.put(i, new byte[128 * 1024]);
            ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
