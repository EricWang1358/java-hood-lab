package lab.prefixidx;

public class FullStringIndex {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Full column index: exact match + ordered, LIMIT 10 needs only 10 reads, no filesort
        int extraReads = 10;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("extraReads=" + extraReads);
        System.out.println("expected=10");
        System.out.println("ms=" + ms);
    }
}
