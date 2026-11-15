package lab.gaplock;

public class RrUniqueMissGap {
    public static void main(String[] args) {
        int probes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int blockedInserts = 0;
        // Existing keys: 5, 10. Query: SELECT * FROM t WHERE id = 7 FOR UPDATE (miss!)
        // RR locks the gap (5, 10) -> any concurrent INSERT into (5, 10) is blocked!
        for (int i = 0; i < probes; i++) {
            int insertId = 6 + (i % 4); // 6, 7, 8, 9 all fall in (5, 10)
            if (insertId > 5 && insertId < 10) {
                blockedInserts++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("blockedInserts=" + blockedInserts);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
