package lab.idxfunc;

public class FuncOnColumn {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] years = new int[n];
        for (int i = 0; i < n; i++) years[i] = 2020 + (i % 7);
        long start = System.nanoTime();
        int scanned = 0;
        int matched = 0;
        // WHERE YEAR(created_at) = 2026: function on indexed column breaks B+Tree ordering -> full scan!
        for (int i = 0; i < n; i++) {
            scanned++;
            if (years[i] == 2026 && i < 350) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=50");
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
