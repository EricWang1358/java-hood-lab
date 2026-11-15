package lab.aslist;

import java.util.ArrayList;
import java.util.List;

public class SubListIndependentCopy {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            List<String> big = new ArrayList<>(List.of("a", "b", "c", "d"));
            List<String> sub = new ArrayList<>(big.subList(0, 2));
            big.clear();
            if (sub.size() == 2) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
