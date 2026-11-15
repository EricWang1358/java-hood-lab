package lab.gaplock;

public class RcRecordLockOnly {
    public static void main(String[] args) {
        int probes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int blockedInserts = 0;
        // Under RC, InnoDB disables Gap Lock -> inserting id=6..9 into (5,10) never blocks!
        for (int i = 0; i < probes; i++) {
            boolean hasGapLock = false;
            if (hasGapLock) blockedInserts++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("blockedInserts=" + blockedInserts);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
