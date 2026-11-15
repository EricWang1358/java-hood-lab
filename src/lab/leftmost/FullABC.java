package lab.leftmost;

public class FullABC {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int matched = Math.max(1, n / 2000);
        long start = System.nanoTime();
        // WHERE a = 5 AND b = 500 AND c = 1: full composite index hit
        int scanned = matched;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=" + matched);
        System.out.println("ms=" + ms);
    }
}
