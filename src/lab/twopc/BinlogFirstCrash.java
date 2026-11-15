package lab.twopc;

import java.util.HashSet;
import java.util.Set;

public class BinlogFirstCrash {
    public static void main(String[] args) {
        int crashes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        Set<Integer> masterRows = new HashSet<>();
        Set<Integer> binlogXids = new HashSet<>();
        int inconsistentCount = 0;

        for (int xid = 1; xid <= crashes; xid++) {
            // Step 1: Flush Binlog first
            binlogXids.add(xid);
            // Crash happens BEFORE Redo commits!
            boolean crashedBeforeRedo = true;
            if (!crashedBeforeRedo) {
                masterRows.add(xid);
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
