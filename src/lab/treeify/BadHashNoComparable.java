package lab.treeify;

import java.util.HashMap;
import java.util.Map;

public class BadHashNoComparable {
    static class BadKey {
        final int id;
        BadKey(int id) { this.id = id; }
        @Override public int hashCode() { return 42; }
        @Override public boolean equals(Object o) {
            return o instanceof BadKey && ((BadKey) o).id == this.id;
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Map<BadKey, Integer> map = new HashMap<>(128);
        BadKey[] keys = new BadKey[n];
        for (int i = 0; i < n; i++) {
            keys[i] = new BadKey(i);
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
