package lab.sqlrank;

import java.util.Arrays;

public class SortRowNumber {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] dept = new int[n];
        int[] sal = new int[n];
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) {
            dept[i] = i % 50;
            sal[i] = (i * 17) % 1000;
            idx[i] = i;
        }
        long start = System.nanoTime();
        Arrays.sort(idx, (a, b) -> dept[a] != dept[b] ? Integer.compare(dept[a], dept[b]) : Integer.compare(sal[b], sal[a]));
        int top3Count = 0;
        int curDept = -1, rn = 0;
        for (int id : idx) {
            if (dept[id] != curDept) { curDept = dept[id]; rn = 1; }
            else rn++;
            if (rn <= 3) top3Count++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("top3=" + top3Count);
        System.out.println("expected=150");
        System.out.println("ms=" + ms);
    }
}
