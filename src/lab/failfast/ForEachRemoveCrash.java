package lab.failfast;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ForEachRemoveCrash {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(i);
        long start = System.nanoTime();
        int ok = 0;
        try {
            for (Integer x : list) {
                if ((x & 1) == 0) list.remove(x);
            }
            ok = list.size();
        } catch (ConcurrentModificationException e) {
            ok = 0;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + (n / 2));
        System.out.println("ms=" + ms);
    }
}
