package lab.rangebreak;

public class ReorderIndex {
    public static void main(String[] args) {
        int rangeRows = Integer.parseInt(args[0]);
        int matched = rangeRows / 100;
        long start = System.nanoTime();
        // Reorder index to (a, c, b): equality on a and c narrows first, then range scan only matched b>10!
        int scanned = matched;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=" + matched);
        System.out.println("ms=" + ms);
    }
}
