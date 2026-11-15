package lab.likeor;

import java.util.Arrays;

public class PrefixLike {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        String[] codes = new String[n];
        for (int i = 0; i < n; i++) codes[i] = String.format("ORD_%06d", i);
        long start = System.nanoTime();
        // LIKE 'ORD_0050%': trailing wildcard stays ordered in B+Tree -> range scan 100 rows!
        int pos = Arrays.binarySearch(codes, "ORD_005000");
        int scanned = 0;
        for (int i = pos; i < n && codes[i].startsWith("ORD_0050"); i++) scanned++;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=100");
        System.out.println("ms=" + ms);
    }
}
