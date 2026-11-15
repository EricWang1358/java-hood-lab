package lab.deeppage;

import java.util.Arrays;

public class CursorSeek {
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
        long[] ids = new long[TOTAL_ROWS];
        String[] payloads = new String[TOTAL_ROWS];
        for (int i = 0; i < TOTAL_ROWS; i++) {
            ids[i] = i + 1L;
            payloads[i] = "order_" + i;
        }
        long lastId = offset; // WHERE id > lastId ORDER BY id LIMIT 20

        long start = System.nanoTime();
        int idx = Arrays.binarySearch(ids, lastId + 1);
        if (idx < 0) idx = -idx - 1;

        int scanned = 0;
        int returned = 0;
        for (int i = idx; i < TOTAL_ROWS && returned < PAGE_SIZE; i++) {
            scanned++;
            rowFetchCost();
            if (payloads[i] != null) returned++;
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=" + PAGE_SIZE);
        System.out.println("returned=" + returned);
        System.out.println("ms=" + ms);
    }
}
