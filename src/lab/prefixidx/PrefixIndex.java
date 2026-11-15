package lab.prefixidx;

import java.util.Arrays;

public class PrefixIndex {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        // Prefix index idx(email(8)): 200 rows share same 8-char prefix -> must back-to-table & filesort
        int samePrefix = Math.max(10, n / 500);
        int[] vals = new int[samePrefix];
        for (int i = 0; i < samePrefix; i++) vals[i] = samePrefix - i;

        long start = System.nanoTime();
        int extraReads = samePrefix; // back-to-table for every prefix collision
        Arrays.sort(vals); // filesort needed because prefix index is not full-column ordered!
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("extraReads=" + extraReads);
        System.out.println("expected=10");
        System.out.println("ms=" + ms);
    }
}
