package lab.covering;

import java.util.Arrays;

public class ExtraColLookup {
    static final int N = 100_000;

    public static void main(String[] args) {
        int q = Integer.parseInt(args[0]);
        int[] pkIds = new int[N];
        String[] phones = new String[N];
        for (int i = 0; i < N; i++) {
            pkIds[i] = i;
            phones[i] = "138" + i;
        }
        long start = System.nanoTime();
        int backToTable = 0;
        for (int i = 0; i < q; i++) {
            // Adding 1 unindexed column `phone` breaks covering index -> back-to-table again!
            int pos = Arrays.binarySearch(pkIds, (i * 31) % N);
            if (pos >= 0 && phones[pos] != null) backToTable++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backToTable=" + backToTable);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
