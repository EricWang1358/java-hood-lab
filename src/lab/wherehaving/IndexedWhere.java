package lab.wherehaving;

public class IndexedWhere {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        int paidCount = rows / 10;
        int[] paidUserIdsSorted = new int[paidCount];
        for (int i = 0; i < paidCount; i++) {
            paidUserIdsSorted[i] = i / Math.max(1, paidCount / 5000);
        }

        long start = System.nanoTime();
        // Index (status, user_id): directly seek status=1 range, stream-group without HashMap
        int groupedRows = 0;
        int keptGroups = 0;
        int prevUser = -1;
        for (int uid : paidUserIdsSorted) {
            groupedRows++;
            if (uid != prevUser) {
                keptGroups++;
                prevUser = uid;
            }
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("groupedRows=" + groupedRows);
        System.out.println("expected=" + paidCount);
        System.out.println("keptGroups=" + keptGroups);
        System.out.println("ms=" + ms);
    }
}
