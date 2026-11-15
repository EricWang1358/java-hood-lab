package lab.compsort;

import java.util.Comparator;

public class SubtractOverflowCmp {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Comparator<Integer> badCmp = (a, b) -> a - b;
        long start = System.nanoTime();
        int orderedOk = 0;
        int min = Integer.MIN_VALUE + 10;
        int max = Integer.MAX_VALUE - 10;
        for (int i = 0; i < n; i++) {
            if (badCmp.compare(min, max) < 0) orderedOk++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("orderedOk=" + orderedOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
