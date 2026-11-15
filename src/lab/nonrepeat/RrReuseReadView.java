package lab.nonrepeat;

public class RrReuseReadView {
    static class UndoNode {
        final int val, trxId;
        final UndoNode prev;
        UndoNode(int val, int trxId, UndoNode prev) {
            this.val = val; this.trxId = trxId; this.prev = prev;
        }
    }

    static int readWithView(UndoNode head, int maxCommittedTrx) {
        UndoNode cur = head;
        while (cur != null && cur.trxId > maxCommittedTrx) cur = cur.prev;
        return cur.val;
    }

    public static void main(String[] args) {
        int rounds = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int inconsistentReads = 0;
        for (int i = 0; i < rounds; i++) {
            UndoNode head = new UndoNode(100, 10, null);
            int firstReadViewMaxTrx = 10;
            int firstRead = readWithView(head, firstReadViewMaxTrx);
            head = new UndoNode(200, 15, head);
            // RR REUSES the ReadView created at first SELECT -> trxId=15 is invisible -> sees 100!
            int secondRead = readWithView(head, firstReadViewMaxTrx);
            if (firstRead != secondRead) inconsistentReads++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("inconsistentReads=" + inconsistentReads);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
