package lab.readview;

public class MvccSnapshotRead {
    static boolean isVisible(int rowTrxId, int creatorTrxId, int[] mIds, int minTrxId, int maxTrxId) {
        if (rowTrxId == creatorTrxId) return true;
        if (rowTrxId < minTrxId) return true;
        if (rowTrxId >= maxTrxId) return false;
        for (int active : mIds) {
            if (active == rowTrxId) return false; // Still active when ReadView was taken
        }
        return true;
    }

    public static void main(String[] args) {
        int reads = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int blockedMs = 0;
        int[] mIds = {100, 102}; // Active writers when ReadView was created
        int minTrxId = 100, maxTrxId = 105, creatorTrxId = 101;
        // Row head is modified by active trx 102, prev undo version is committed trx 90
        int headTrx = 102, prevTrx = 90;
        int visibleReads = 0;
        for (int i = 0; i < reads; i++) {
            int chosenTrx = isVisible(headTrx, creatorTrxId, mIds, minTrxId, maxTrxId) ? headTrx : prevTrx;
            if (chosenTrx == 90) visibleReads++; // Zero lock wait!
        }
        long ms = (System.nanoTime() - start) / 1_000_000 + blockedMs;
        System.out.println("blockedMs=" + blockedMs);
        System.out.println("expected=0");
        System.out.println("visibleReads=" + visibleReads);
        System.out.println("ms=" + ms);
    }
}
