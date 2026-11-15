package lab.covering;

import java.util.Arrays;

public class SelectStarLookup {
    static final int N = 100_000;

    public static void main(String[] args) {
        int q = Integer.parseInt(args[0]);
        int[] pkIds = new int[N];
        String[] fullRows = new String[N];
        for (int i = 0; i < N; i++) {
            pkIds[i] = i;
            fullRows[i] = "full_payload_" + i;
        }
        long start = System.nanoTime();
        int backToTable = 0;
        for (int i = 0; i < q; i++) {
            int pk = (i * 31) % N;
            // SELECT * needs columns not in secondary index -> must back-to-table!
            int pos = Arrays.binarySearch(pkIds, pk);
            if (pos >= 0 && fullRows[pos] != null) backToTable++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backToTable=" + backToTable);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
