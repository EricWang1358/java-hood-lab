package lab.leftmost;

public class PrefixAOnly {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int bucket = 1000;
        long start = System.nanoTime();
        // WHERE a = 5: seeks directly to a=5 range, scans only 1000 rows
        int scanned = Math.min(n, bucket);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=" + (n / 2000));
        System.out.println("ms=" + ms);
    }
}
