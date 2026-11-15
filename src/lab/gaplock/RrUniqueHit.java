package lab.gaplock;

public class RrUniqueHit {
    public static void main(String[] args) {
        int probes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int blockedInserts = 0;
        // Query hits existing unique key id = 5 -> Next-Key degrades to Record Lock on id=5 only!
        int lockedRecord = 5;
        for (int i = 0; i < probes; i++) {
            int insertId = 6 + (i % 4);
            if (insertId == lockedRecord) {
                blockedInserts++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("blockedInserts=" + blockedInserts);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
