package lab.lookup;

public class CoveringOnly {
    static final int N = 100_000;

    public static void main(String[] args) {
        int hits = Integer.parseInt(args[0]);
        int[] secIndexLeaf = new int[N];
        for (int i = 0; i < N; i++) secIndexLeaf[i] = i;

        long start = System.nanoTime();
        int treeLookups = 0;
        int found = 0;
        for (int i = 0; i < hits; i++) {
            // Covering index: needed column is already in secondary index leaf -> 0 back-to-table!
            int val = secIndexLeaf[(i * 19) % N];
            treeLookups++;
            if (val >= 0) found++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("treeLookups=" + treeLookups);
        System.out.println("expected=" + hits);
        System.out.println("found=" + found);
        System.out.println("ms=" + ms);
    }
}
