package lab.dirtyread;

public class ReadUncommitted {
    static class UndoNode {
        final int val, trxId;
        final UndoNode prev;
        UndoNode(int val, int trxId, UndoNode prev) {
            this.val = val; this.trxId = trxId; this.prev = prev;
        }
    }

    public static void main(String[] args) {
        int rounds = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int dirtyReads = 0;
        for (int i = 0; i < rounds; i++) {
            UndoNode committed = new UndoNode(100, 10, null);
            // Writer (trxId=20) writes 900 in-place, not committed yet (will rollback!)
            UndoNode head = new UndoNode(900, 20, committed);
            // Read Uncommitted ignores trxId and ReadView -> reads head.val directly!
            int seen = head.val;
            // Writer rolls back to committed.val (100) -> reader saw fake 900!
            if (seen != committed.val) dirtyReads++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("dirtyReads=" + dirtyReads);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
