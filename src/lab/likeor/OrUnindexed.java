package lab.likeor;

public class OrUnindexed {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] colA = new int[n];
        int[] colB = new int[n];
        for (int i = 0; i < n; i++) { colA[i] = i; colB[i] = i % 1000; }
        long start = System.nanoTime();
        int scanned = 0;
        int matched = 0;
        // WHERE a = 1 OR b = 2 (b has NO index): MySQL cannot use index for a alone, full table scan!
        for (int i = 0; i < n; i++) {
            scanned++;
            if (colA[i] == 1 || colB[i] == 2) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=100");
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
