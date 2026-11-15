package lab.stringtable;

import java.util.HashMap;
import java.util.Map;

public class BoundedInternCache {
    static volatile String sink;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        String[] canonical = new String[100];
        Map<String, String> cache = new HashMap<>(256);
        for (int i = 0; i < 100; i++) {
            canonical[i] = ("key-" + i).intern();
            cache.put(canonical[i], canonical[i]);
        }

        long start = System.nanoTime();
        int dedupOk = 0;
        for (int i = 0; i < n; i++) {
            int idx = i % 100;
            String raw = "key-" + idx;
            String s = cache.get(raw);
            if (s == canonical[idx]) dedupOk++;
            sink = s;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("dedupOk=" + dedupOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
