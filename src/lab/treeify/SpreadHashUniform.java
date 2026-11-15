package lab.treeify;

import java.util.HashMap;
import java.util.Map;

public class SpreadHashUniform {
    static class GoodKey {
        final int id;
        GoodKey(int id) { this.id = id; }
        @Override public int hashCode() { return Integer.hashCode(id); }
        @Override public boolean equals(Object o) {
            return o instanceof GoodKey && ((GoodKey) o).id == this.id;
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Map<GoodKey, Integer> map = new HashMap<>(n * 2);
        GoodKey[] keys = new GoodKey[n];
        for (int i = 0; i < n; i++) {
            keys[i] = new GoodKey(i);
            map.put(keys[i], i);
        }
        long start = System.nanoTime();
        int hits = 0;
        for (int i = 0; i < n; i++) {
            if (map.get(keys[i]) != null) hits++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("hits=" + hits);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
