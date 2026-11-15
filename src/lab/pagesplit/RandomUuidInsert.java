package lab.pagesplit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomUuidInsert {
    static final int PAGE_CAP = 64;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Random rnd = new Random(42);
        List<Integer> pages = new ArrayList<>();
        pages.add(0);
        int splits = 0;

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            int key = rnd.nextInt(); // random UUID-like key lands in random page
            int p = rnd.nextInt(pages.size());
            int sz = pages.get(p) + 1;
            if (sz > PAGE_CAP) {
                pages.set(p, PAGE_CAP / 2);
                pages.add(p + 1, PAGE_CAP / 2 + 1);
                splits++;
            } else {
                pages.set(p, sz);
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("splits=" + splits);
        System.out.println("expected=0");
        System.out.println("pages=" + pages.size());
        System.out.println("ms=" + ms);
    }
}
