package lab.twopc;

import java.util.HashSet;
import java.util.Set;

public class RedoFirstCrash {
    public static void main(String[] args) {
        int crashes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        Set<Integer> masterRows = new HashSet<>();
        Set<Integer> binlogXids = new HashSet<>();
        int inconsistentCount = 0;

        for (int xid = 1; xid <= crashes; xid++) {
            // Step 1: Commit Redo directly
            masterRows.add(xid);
            // Crash happens BEFORE writing Binlog!
            boolean crashedBeforeBinlog = true;
            if (!crashedBeforeBinlog) {
                binlogXids.add(xid);
            }
            // Replica only replays binlogXids -> Master has xid, Replica does NOT!
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
