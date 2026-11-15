package lab.gaplock;

public class RrRangeNextKey {
    public static void main(String[] args) {
        int probes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int blockedInserts = 0;
        // Range query id >= 5 AND id < 10 acquires Next-Key Lock on (5, 10]
        for (int i = 0; i < probes; i++) {
            int insertId = 6 + (i % 4);
            if (insertId > 5 && insertId <= 10) {
                blockedInserts++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("blockedInserts=" + blockedInserts);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
