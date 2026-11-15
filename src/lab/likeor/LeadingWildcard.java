package lab.likeor;

public class LeadingWildcard {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        String[] codes = new String[n];
        for (int i = 0; i < n; i++) codes[i] = "ORD_" + i;
        long start = System.nanoTime();
        int scanned = 0;
        int matched = 0;
        // LIKE '%999': leading wildcard cannot use B+Tree prefix order -> full scan!
        for (int i = 0; i < n; i++) {
            scanned++;
            if (codes[i].endsWith("999")) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=100");
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
