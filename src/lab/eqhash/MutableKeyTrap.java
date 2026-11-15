package lab.eqhash;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MutableKeyTrap {
    static class MutableKey {
        int id;
        MutableKey(int id) { this.id = id; }

        @Override
        public boolean equals(Object o) {
            return o instanceof MutableKey && ((MutableKey) o).id == this.id;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Map<MutableKey, Integer> map = new HashMap<>(n * 2);
        MutableKey[] keys = new MutableKey[n];
        for (int i = 0; i < n; i++) {
            keys[i] = new MutableKey(i);
            map.put(keys[i], i);
            keys[i].id = i + 1_000_000;
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
