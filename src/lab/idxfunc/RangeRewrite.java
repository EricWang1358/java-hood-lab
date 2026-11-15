package lab.idxfunc;

import java.util.Arrays;

public class RangeRewrite {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] days = new int[n];
        for (int i = 0; i < n; i++) days[i] = i;
        long start = System.nanoTime();
        // Keep indexed column clean: WHERE created_at >= start AND created_at < end
        int pos = Arrays.binarySearch(days, 1000);
        int scanned = 0;
        for (int i = pos; i < n && i < pos + 50; i++) scanned++;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=50");
        System.out.println("ms=" + ms);
    }
}
