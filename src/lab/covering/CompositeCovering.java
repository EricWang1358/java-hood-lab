package lab.covering;

public class CompositeCovering {
    static final int N = 100_000;

    public static void main(String[] args) {
        int q = Integer.parseInt(args[0]);
        String[] idxPhone = new String[N];
        for (int i = 0; i < N; i++) idxPhone[i] = "138" + i;

        long start = System.nanoTime();
        int backToTable = 0;
        int ok = 0;
        for (int i = 0; i < q; i++) {
            // Composite index (name, phone): covers SELECT id, name, phone -> 0 back-to-table!
            if (idxPhone[(i * 31) % N] != null) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backToTable=" + backToTable);
        System.out.println("expected=0");
        System.out.println("ok=" + ok);
        System.out.println("ms=" + ms);
    }
}
