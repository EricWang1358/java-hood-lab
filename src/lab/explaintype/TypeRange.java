package lab.explaintype;

import java.util.Arrays;

public class TypeRange {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] idx = new int[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        long start = System.nanoTime();
        // type=range: seek to 500, scan only [500, 510)
        int pos = Arrays.binarySearch(idx, 500);
        int scanned = 0;
        for (int i = pos; i < n && idx[i] < 510; i++) scanned++;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=10");
        System.out.println("ms=" + ms);
    }
}
