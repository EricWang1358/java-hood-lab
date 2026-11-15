package lab.lookup;

import java.util.Arrays;

public class ClusteredPk {
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
            int pk = (i * 19) % N;
            // Direct clustered index lookup: row payload is right in the leaf!
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
