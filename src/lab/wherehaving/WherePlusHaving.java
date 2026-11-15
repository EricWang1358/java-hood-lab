package lab.wherehaving;

import java.util.HashMap;
import java.util.Map;

public class WherePlusHaving {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        int[] userIds = new int[rows];
        int[] status = new int[rows];
        for (int i = 0; i < rows; i++) {
            userIds[i] = i % 5000;
            status[i] = (i % 10 == 0) ? 1 : 0;
        }

        long start = System.nanoTime();
        // Best practice: WHERE filters rows, HAVING filters aggregated groups (COUNT(*) >= 3)
        Map<Integer, Integer> groupMap = new HashMap<>();
        int groupedRows = 0;
        for (int i = 0; i < rows; i++) {
            if (status[i] != 1) continue;
            groupedRows++;
            groupMap.merge(userIds[i], 1, Integer::sum);
        }
        int keptGroups = 0;
        for (int cnt : groupMap.values()) {
            if (cnt >= 3) keptGroups++;
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("groupedRows=" + groupedRows);
        System.out.println("expected=" + (rows / 10));
        System.out.println("keptGroups=" + keptGroups);
        System.out.println("ms=" + ms);
    }
}
