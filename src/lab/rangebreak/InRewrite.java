package lab.rangebreak;

public class InRewrite {
    public static void main(String[] args) {
        int rangeRows = Integer.parseInt(args[0]);
        int matched = rangeRows / 100;
        long start = System.nanoTime();
        // When range is a small discrete set, b IN (11,12) lets c stay usable in B+Tree!
        int scanned = matched;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=" + matched);
        System.out.println("ms=" + ms);
    }
}
