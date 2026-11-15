package lab.phantom;

import java.util.ArrayList;
import java.util.List;

public class RrSnapshotPlusUpdate {
    static class Row {
        int id, trxId, ownerTag;
        Row(int id, int trxId, int ownerTag) {
            this.id = id; this.trxId = trxId; this.ownerTag = ownerTag;
        }
    }

    public static void main(String[] args) {
        int rounds = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int phantomHits = 0;
        for (int i = 0; i < rounds; i++) {
            List<Row> table = new ArrayList<>();
            for (int k = 1; k <= 5; k++) table.add(new Row(k, 10, 0));
            int myTrxId = 20;
            // Step 1: Snapshot SELECT sees 5 rows (trxId <= 10)
            int firstCount = 0;
            for (Row r : table) if (r.trxId <= 10) firstCount++;
            // Step 2: Another tx (trxId=25) inserts & commits Row 6!
            table.add(new Row(6, 25, 0));
            // Step 3: Current-read UPDATE touches ALL rows and overwrites trxId = myTrxId (20)!
            for (Row r : table) { r.ownerTag = 1; r.trxId = myTrxId; }
            // Step 4: Second Snapshot SELECT sees Row 6 because its trxId == myTrxId!
            int secondCount = 0;
            for (Row r : table) if (r.trxId <= 10 || r.trxId == myTrxId) secondCount++;
            if (secondCount != firstCount) phantomHits++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("phantomHits=" + phantomHits);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
