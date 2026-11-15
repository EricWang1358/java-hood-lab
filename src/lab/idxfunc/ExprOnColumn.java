package lab.idxfunc;

public class ExprOnColumn {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] ids = new int[n];
        for (int i = 0; i < n; i++) ids[i] = i;
        long start = System.nanoTime();
        int scanned = 0;
        int matched = 0;
        // WHERE id + 1 = 100: arithmetic on indexed column prevents B+Tree seek!
        for (int i = 0; i < n; i++) {
            scanned++;
            if (ids[i] + 1 == 100) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=50");
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
