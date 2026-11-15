package lab.explaintype;

public class TypeAll {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] rows = new int[n];
        for (int i = 0; i < n; i++) rows[i] = i;
        long start = System.nanoTime();
        int scanned = 0;
        int matched = 0;
        // type=ALL: full table scan
        for (int i = 0; i < n; i++) {
            scanned++;
            if (rows[i] >= 500 && rows[i] < 510) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=10");
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
