package lab.wherehaving;

import java.util.HashMap;
import java.util.Map;

public class WhereBeforeGroup {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        int[] userIds = new int[rows];
        int[] status = new int[rows];
        for (int i = 0; i < rows; i++) {
            userIds[i] = i % 5000;
            status[i] = (i % 10 == 0) ? 1 : 0;
        }

        long start = System.nanoTime();
        // WHERE status = 1 filters out 90% BEFORE GROUP BY
        Map<Integer, Integer> groupMap = new HashMap<>();
        int groupedRows = 0;
        for (int i = 0; i < rows; i++) {
            if (status[i] != 1) continue;
            groupedRows++;
            groupMap.merge(userIds[i], 1, Integer::sum);
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("groupedRows=" + groupedRows);
        System.out.println("expected=" + (rows / 10));
        System.out.println("keptGroups=" + groupMap.size());
        System.out.println("ms=" + ms);
    }
}
