package lab.wherehaving;

import java.util.HashMap;
import java.util.Map;

public class HavingFilter {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        int[] userIds = new int[rows];
        int[] status = new int[rows]; // 1 = PAID (10%), 0 = OTHER (90%)
        for (int i = 0; i < rows; i++) {
            userIds[i] = i % 5000;
            status[i] = (i % 10 == 0) ? 1 : 0;
        }

        long start = System.nanoTime();
        // GROUP BY (user_id, status) first, then HAVING status = 1
        Map<Long, Integer> groupMap = new HashMap<>();
        int groupedRows = 0;
        for (int i = 0; i < rows; i++) {
            groupedRows++;
            long key = ((long) userIds[i] << 1) | status[i];
            groupMap.merge(key, 1, Integer::sum);
        }
        int keptGroups = 0;
        for (long key : groupMap.keySet()) {
            if ((key & 1L) == 1L) keptGroups++;
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("groupedRows=" + groupedRows);
        System.out.println("expected=" + (rows / 10));
        System.out.println("keptGroups=" + keptGroups);
        System.out.println("ms=" + ms);
    }
}
