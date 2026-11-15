package lab.lookup;

import java.util.Arrays;

public class SecondaryLookup {
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
        int treeLookups = 0;
        int found = 0;
        for (int i = 0; i < hits; i++) {
            // 1. Search secondary index to get pk
            int pk = (i * 19) % N;
            treeLookups++;
            // 2. Back-to-table (回表): binary search clustered index by pk
            int pos = Arrays.binarySearch(pkIds, pk);
            treeLookups++;
            if (pos >= 0 && rows[pos] != null) found++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("treeLookups=" + treeLookups);
        System.out.println("expected=" + hits);
        System.out.println("found=" + found);
        System.out.println("ms=" + ms);
    }
}
