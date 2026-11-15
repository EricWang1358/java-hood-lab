package lab.gaplock;

public class RrUniqueMissGap {
    public static void main(String[] args) {
        int probes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int[] existingKeys = {5, 10, 15, 20};
        int queryKey = 7; // Does NOT exist in table!
        // Locate surrounding keys (left=5, right=10) -> RR locks open gap (5, 10)
        int gapLeft = Integer.MIN_VALUE, gapRight = Integer.MAX_VALUE;
        for (int k : existingKeys) {
            if (k < queryKey) gapLeft = k;
            if (k > queryKey && gapRight == Integer.MAX_VALUE) gapRight = k;
        }
        int blockedInserts = 0;
        for (int i = 0; i < probes; i++) {
            int insertId = 6 + (i % 4); // 6, 7, 8, 9
            if (insertId > gapLeft && insertId < gapRight) {
                blockedInserts++; // Blocked by Gap Lock (5, 10)!
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("blockedInserts=" + blockedInserts);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
