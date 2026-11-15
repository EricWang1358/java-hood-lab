package lab.longtrx;

import java.util.ArrayList;
import java.util.List;

public class LongTxHoldingView {
    public static void main(String[] args) {
        int updates = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        List<Integer> undoVersions = new ArrayList<>(updates);
        int oldestActiveReadViewTrx = 1; // A forgotten long transaction started at trxId=1!
        for (int trxId = 2; trxId <= updates + 1; trxId++) {
            undoVersions.add(trxId);
        }
        // Purge thread can only reclaim versions with trxId < oldestActiveReadViewTrx
        int unpurgedUndo = 0;
        for (int vTrx : undoVersions) {
            if (vTrx >= oldestActiveReadViewTrx) unpurgedUndo++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("unpurgedUndo=" + unpurgedUndo);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
