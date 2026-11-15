package lab.aslist;

import java.util.Arrays;
import java.util.List;

public class ArraysAsListAddCrash {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            List<String> list = Arrays.asList("a", "b");
            try {
                list.add("c");
                ok++;
            } catch (UnsupportedOperationException ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
