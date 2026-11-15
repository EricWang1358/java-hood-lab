package lab.pagesplit;

import java.util.ArrayList;
import java.util.List;

public class AutoIncAppend {
    static final int PAGE_CAP = 64;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        List<Integer> pages = new ArrayList<>();
        pages.add(0);
        int splits = 0;

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            int last = pages.size() - 1;
            int sz = pages.get(last);
            if (sz >= PAGE_CAP) {
                // Sequential append: allocate new leaf page at tail, zero mid-page split!
                pages.add(1);
            } else {
                pages.set(last, sz + 1);
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("splits=" + splits);
        System.out.println("expected=0");
        System.out.println("pages=" + pages.size());
        System.out.println("ms=" + ms);
    }
}
