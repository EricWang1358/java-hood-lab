package lab.deeppage;

public class OffsetScan {
    static final int TOTAL_ROWS = 400_000;
    static final int PAGE_SIZE = 20;

    static void rowFetchCost() {
        long until = System.nanoTime() + 150L; // simulate clustered-page row lookup
        while (System.nanoTime() < until) {
            Thread.onSpinWait();
        }
    }

    public static void main(String[] args) {
        int offset = Integer.parseInt(args[0]);
        long[] ids = new long[TOTAL_ROWS];
        String[] payloads = new String[TOTAL_ROWS];
        for (int i = 0; i < TOTAL_ROWS; i++) {
            ids[i] = i + 1L;
            payloads[i] = "order_" + i;
        }

        long start = System.nanoTime();
        int scanned = 0;
        int returned = 0;
        for (int i = 0; i < TOTAL_ROWS && i < offset + PAGE_SIZE; i++) {
            scanned++;
            rowFetchCost(); // MySQL fetches full row & discards first `offset` rows
            if (i >= offset) {
                if (payloads[i] != null && ids[i] > 0) returned++;
            }
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=" + PAGE_SIZE);
        System.out.println("returned=" + returned);
        System.out.println("ms=" + ms);
    }
}
