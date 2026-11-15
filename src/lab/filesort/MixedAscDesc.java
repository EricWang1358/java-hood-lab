package lab.filesort;

import java.util.Arrays;

public class MixedAscDesc {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] vals = new int[n];
        for (int i = 0; i < n; i++) vals[i] = n - i;

        long start = System.nanoTime();
        // ORDER BY a ASC, b DESC on old (a, b) index: opposite directions trigger filesort!
        Arrays.sort(vals);
        int sortedRows = n;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("sortedRows=" + sortedRows);
        System.out.println("expected=20");
        System.out.println("ms=" + ms);
    }
}
