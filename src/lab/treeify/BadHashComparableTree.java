package lab.treeify;

import java.util.HashMap;
import java.util.Map;

public class BadHashComparableTree {
    static class CmpKey implements Comparable<CmpKey> {
        final int id;
        CmpKey(int id) { this.id = id; }
        @Override public int hashCode() { return 42; }
        @Override public boolean equals(Object o) {
            return o instanceof CmpKey && ((CmpKey) o).id == this.id;
        }
        @Override public int compareTo(CmpKey o) {
            return Integer.compare(this.id, o.id);
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Map<CmpKey, Integer> map = new HashMap<>(128);
        CmpKey[] keys = new CmpKey[n];
        for (int i = 0; i < n; i++) {
            keys[i] = new CmpKey(i);
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
