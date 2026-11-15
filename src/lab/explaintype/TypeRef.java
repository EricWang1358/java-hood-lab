package lab.explaintype;

import java.util.Arrays;

public class TypeRef {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] idx = new int[n];
        for (int i = 0; i < n; i++) idx[i] = i / 10; // 10 rows per key
        long start = System.nanoTime();
        // type=ref: equality lookup on non-unique index
        int pos = Arrays.binarySearch(idx, 50);
        int scanned = 0;
        int l = pos;
        while (l >= 0 && idx[l] == 50) { scanned++; l--; }
        int r = pos + 1;
        while (r < n && idx[r] == 50) { scanned++; r++; }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=10");
        System.out.println("ms=" + ms);
    }
}
