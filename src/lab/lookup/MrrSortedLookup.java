package lab.lookup;

import java.util.Arrays;

public class MrrSortedLookup {
    static final int N = 100_000;

    public static void main(String[] args) {
        int hits = Integer.parseInt(args[0]);
        int[] pkIds = new int[N];
        String[] rows = new String[N];
        for (int i = 0; i < N; i++) {
            pkIds[i] = i;
            rows[i] = "row_" + i;
        }
        long start = System.nanoTime();
        int[] batchPks = new int[hits];
        for (int i = 0; i < hits; i++) batchPks[i] = (i * 19) % N;
        // MRR: sort PKs before back-to-table so disk I/O becomes sequential!
        Arrays.sort(batchPks);
        int treeLookups = hits;
        int found = 0;
        for (int pk : batchPks) {
            if (rows[pk] != null) found++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("treeLookups=" + treeLookups);
        System.out.println("expected=" + hits);
        System.out.println("found=" + found);
        System.out.println("ms=" + ms);
    }
}
