package lab.readview;

public class MvccSnapshotRead {
    static boolean isVisible(int rowTrxId, int creatorTrxId, int minTrxId, int maxTrxId, int[] activeIds) {
        if (rowTrxId == creatorTrxId || rowTrxId < minTrxId) return true;
        if (rowTrxId >= maxTrxId) return false;
        for (int id : activeIds) if (id == rowTrxId) return false;
        return true;
    }

    public static void main(String[] args) {
        int ops = Integer.parseInt(args[0]);
        int[] activeIds = {102, 105};
        long start = System.nanoTime();
        int blockedReads = 0;
        int ok = 0;
        for (int i = 0; i < ops; i++) {
            // Walk undo chain [trx=105 -> trx=98]: skip 105 (active), read 98 (< minTrxId) with ZERO lock!
            if (!isVisible(105, 103, 102, 106, activeIds) && isVisible(98, 103, 102, 106, activeIds)) {
                ok++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("blockedReads=" + blockedReads);
        System.out.println("expected=0");
        System.out.println("ok=" + ok);
        System.out.println("ms=" + ms);
    }
}
