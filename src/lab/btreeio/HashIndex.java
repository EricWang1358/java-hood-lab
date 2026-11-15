package lab.btreeio;

import java.util.HashMap;
import java.util.Map;

public class HashIndex {
    static final int TOTAL = 200_000;

    public static void main(String[] args) {
        int range = Integer.parseInt(args[0]);
        Map<Integer, Integer> map = new HashMap<>(TOTAL * 2);
        for (int i = 0; i < TOTAL; i++) map.put(i, i);

        long start = System.nanoTime();
        // Hash index cannot do range scan (WHERE id BETWEEN 1000 AND 1000+range); falls back to full scan!
        int pageReads = 0;
        int matched = 0;
        for (int k : map.keySet()) {
            pageReads++;
            if (k >= 1000 && k < 1000 + range) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("pageReads=" + (pageReads / 100));
        System.out.println("expected=" + (3 + Math.max(1, range / 100)));
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
