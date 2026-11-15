package lab.eqhash;

import java.util.HashMap;
import java.util.Map;

public class EqualsOnlyLostKey {
    static class Key {
        final int id;
        Key(int id) { this.id = id; }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key && ((Key) o).id == this.id;
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Map<Key, Integer> map = new HashMap<>(n * 2);
        for (int i = 0; i < n; i++) map.put(new Key(i), i);
        long start = System.nanoTime();
        int hits = 0;
        for (int i = 0; i < n; i++) {
            if (map.get(new Key(i)) != null) hits++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("hits=" + hits);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
