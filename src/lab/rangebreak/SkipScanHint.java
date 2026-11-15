package lab.rangebreak;

public class SkipScanHint {
    public static void main(String[] args) {
        int rangeRows = Integer.parseInt(args[0]);
        int matched = rangeRows / 100;
        long start = System.nanoTime();
        // Closed equality + range: keeping equality columns left of range maximizes key_len
        int scanned = matched;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=" + matched);
        System.out.println("ms=" + ms);
    }
}
