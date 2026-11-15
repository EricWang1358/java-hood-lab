package lab.twopc;

import java.util.HashSet;
import java.util.Set;

public class TwoPhaseCommit {
    public static void main(String[] args) {
        int crashes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        Set<Integer> redoPrepared = new HashSet<>();
        Set<Integer> masterRows = new HashSet<>();
        Set<Integer> binlogXids = new HashSet<>();
        int inconsistentCount = 0;

        for (int xid = 1; xid <= crashes; xid++) {
            redoPrepared.add(xid);           // Phase 1: Redo Prepare (with XID)
            if (xid % 2 == 0) {
                binlogXids.add(xid);         // Phase 2: Binlog flushed completely
            }
            // Crash happens here before Phase 3!
            // Recovery: inspect Redo Prepared XIDs against Binlog
            if (redoPrepared.contains(xid)) {
                if (binlogXids.contains(xid)) {
                    masterRows.add(xid);     // Binlog complete -> Commit Redo!
                } else {
                    redoPrepared.remove(xid); // Binlog missing -> Rollback Redo!
                }
            }
            if (masterRows.contains(xid) != binlogXids.contains(xid)) {
                inconsistentCount++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("inconsistentCount=" + inconsistentCount);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
