package lab.explaintype;

public class TypeIndex {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] idxKeys = new int[n];
        for (int i = 0; i < n; i++) idxKeys[i] = i;
        long start = System.nanoTime();
        int scanned = 0;
        int matched = 0;
        // type=index: full secondary index scan (smaller than ALL, still O(N))
        for (int i = 0; i < n; i++) {
            scanned++;
            if (idxKeys[i] >= 500 && idxKeys[i] < 510) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=10");
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
