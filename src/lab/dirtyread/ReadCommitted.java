package lab.dirtyread;

public class ReadCommitted {
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
        int activeWriterTrx = 20;
        for (int i = 0; i < rounds; i++) {
            UndoNode committed = new UndoNode(100, 10, null);
            UndoNode head = new UndoNode(900, 20, committed);
            // RC checks ReadView: trxId 20 is still active -> follow roll_ptr (prev)!
            UndoNode cur = head;
            while (cur != null && cur.trxId == activeWriterTrx) {
                cur = cur.prev;
            }
            int seen = (cur != null) ? cur.val : 0;
            if (seen != committed.val) dirtyReads++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("dirtyReads=" + dirtyReads);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
