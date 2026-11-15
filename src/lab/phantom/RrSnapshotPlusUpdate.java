package lab.phantom;

public class RrSnapshotPlusUpdate {
    public static void main(String[] args) {
        int rounds = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int phantomHits = 0;
        for (int i = 0; i < rounds; i++) {
            int firstCount = 5;
            // Tx B inserts 1 new row (trx=200) and commits.
            // Tx A (trx=100) runs UPDATE (current read!), changing new row's trx_id from 200 -> 100!
            // Next snapshot SELECT in Tx A sees trx_id == creatorTrxId(100) -> 6 rows! Phantom!
            int afterUpdateCount = 6;
            if (afterUpdateCount != firstCount) phantomHits++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("phantomHits=" + phantomHits);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
