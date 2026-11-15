package lab.compsort;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class TreeSetTieBreakerSafe {
    static class Item {
        final int score;
        final int id;
        Item(int score, int id) { this.score = score; this.id = id; }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        Comparator<Item> cmp = Comparator.comparingInt((Item it) -> it.score)
                .thenComparingInt(it -> it.id);
        Set<Item> set = new TreeSet<>(cmp);
        for (int i = 0; i < n; i++) {
            set.add(new Item(100, i));
        }
        int orderedOk = set.size();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("orderedOk=" + orderedOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
