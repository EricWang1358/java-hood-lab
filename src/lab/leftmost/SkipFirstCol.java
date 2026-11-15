package lab.leftmost;

public class SkipFirstCol {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] colA = new int[n];
        int[] colB = new int[n];
        for (int i = 0; i < n; i++) {
            colA[i] = i / 1000;
            colB[i] = i % 2000;
        }
        long start = System.nanoTime();
        int scanned = 0;
        int matched = 0;
        // WHERE b = 500 on index(a, b, c): skips leading column `a`, must full-scan!
        for (int i = 0; i < n; i++) {
            scanned++;
            if (colB[i] == 500) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=" + matched);
        System.out.println("ms=" + ms);
    }
}
