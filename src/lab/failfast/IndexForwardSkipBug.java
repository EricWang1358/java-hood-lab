package lab.failfast;

import java.util.ArrayList;
import java.util.List;

public class IndexForwardSkipBug {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(0);
        long start = System.nanoTime();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == 0) {
                list.remove(i);
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        int removed = n - list.size();
        System.out.println("ok=" + removed);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
