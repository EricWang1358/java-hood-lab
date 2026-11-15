package lab.rangebreak;

public class RangeBeforeEq {
    public static void main(String[] args) {
        int rangeRows = Integer.parseInt(args[0]);
        int[] colC = new int[rangeRows];
        for (int i = 0; i < rangeRows; i++) colC[i] = i % 100;

        long start = System.nanoTime();
        int scanned = 0;
        int matched = 0;
        // Index (a, b, c) with WHERE a=1 AND b>10 AND c=2: range on b breaks ordering of c!
        for (int i = 0; i < rangeRows; i++) {
            scanned++;
            if (colC[i] == 2) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=" + (rangeRows / 100));
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
