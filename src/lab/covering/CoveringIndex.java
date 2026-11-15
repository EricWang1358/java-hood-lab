package lab.covering;

public class CoveringIndex {
    static final int N = 100_000;

    public static void main(String[] args) {
        int q = Integer.parseInt(args[0]);
        String[] idxNames = new String[N];
        for (int i = 0; i < N; i++) idxNames[i] = "u_" + i;

        long start = System.nanoTime();
        int backToTable = 0;
        int ok = 0;
        for (int i = 0; i < q; i++) {
            // SELECT id, name: both in idx(name), Using index -> 0 back-to-table!
            if (idxNames[(i * 31) % N] != null) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backToTable=" + backToTable);
        System.out.println("expected=0");
        System.out.println("ok=" + ok);
        System.out.println("ms=" + ms);
    }
}
