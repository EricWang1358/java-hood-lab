package lab.longtrx;

import java.util.ArrayList;
import java.util.List;

public class ShortTxPurge {
    public static void main(String[] args) {
        int updates = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        List<Integer> undoVersions = new ArrayList<>(updates);
        for (int trxId = 2; trxId <= updates + 1; trxId++) {
            undoVersions.add(trxId);
        }
        // Short transactions committed promptly -> oldestActiveReadViewTrx advances to latest!
        int oldestActiveReadViewTrx = updates + 2;
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
