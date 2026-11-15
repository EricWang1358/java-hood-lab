package lab.deeppage;

public class DeferredJoin {
    static final int TOTAL_ROWS = 400_000;
    static final int PAGE_SIZE = 20;

    static void rowFetchCost() {
        long until = System.nanoTime() + 150L;
        while (System.nanoTime() < until) {
            Thread.onSpinWait();
        }
    }

    public static void main(String[] args) {
        int offset = Integer.parseInt(args[0]);
        long[] indexIds = new long[TOTAL_ROWS];
        String[] payloads = new String[TOTAL_ROWS];
        for (int i = 0; i < TOTAL_ROWS; i++) {
            indexIds[i] = i + 1L;
            payloads[i] = "order_" + i;
        }

        long start = System.nanoTime();
        // Step 1: scan only covering index (no row payload fetch)
        long[] targetIds = new long[PAGE_SIZE];
        int k = 0;
        for (int i = 0; i < TOTAL_ROWS && i < offset + PAGE_SIZE; i++) {
            if (i >= offset) targetIds[k++] = indexIds[i];
        }
        // Step 2: join back to clustered index only for the 20 matched rows
        int rowLookups = 0;
        int returned = 0;
        for (int i = 0; i < k; i++) {
            rowLookups++;
            rowFetchCost();
            int pos = (int) (targetIds[i] - 1);
            if (payloads[pos] != null) returned++;
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + rowLookups);
        System.out.println("expected=" + PAGE_SIZE);
        System.out.println("returned=" + returned);
        System.out.println("ms=" + ms);
    }
}
