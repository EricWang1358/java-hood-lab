package lab.filesort;

import java.util.Arrays;

public class FilesortUnindexed {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] times = new int[n];
        for (int i = 0; i < n; i++) times[i] = n - i;

        long start = System.nanoTime();
        // ORDER BY created_at without matching index: must load & sort ALL n rows before LIMIT 20!
        Arrays.sort(times);
        int sortedRows = n;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("sortedRows=" + sortedRows);
        System.out.println("expected=20");
        System.out.println("ms=" + ms);
    }
}
