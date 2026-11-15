package lab.arraygrow;

import java.util.LinkedList;
import java.util.List;

public class LinkedListRandomGet {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < n; i++) list.add(i);
        long start = System.nanoTime();
        long sum = 0;
        for (int i = 0; i < n; i += 4) {
            sum += list.get(i);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("count=" + (sum > 0 ? n : 0));
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
