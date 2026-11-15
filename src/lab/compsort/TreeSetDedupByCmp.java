package lab.compsort;

import java.util.Set;
import java.util.TreeSet;

public class TreeSetDedupByCmp {
    static class Item {
        final int score;
        final int id;
        Item(int score, int id) { this.score = score; this.id = id; }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int orderedOk = 0;
        Set<Item> set = new TreeSet<>((a, b) -> Integer.compare(a.score, b.score));
        for (int i = 0; i < n; i++) {
            set.add(new Item(100, i));
        }
        orderedOk = set.size();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("orderedOk=" + orderedOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
