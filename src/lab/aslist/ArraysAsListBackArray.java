package lab.aslist;

import java.util.Arrays;
import java.util.List;

public class ArraysAsListBackArray {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int isolated = 0;
        for (int i = 0; i < n; i++) {
            String[] arr = new String[]{"a", "b"};
            List<String> list = Arrays.asList(arr);
            list.set(0, "Z");
            if ("a".equals(arr[0])) isolated++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + isolated);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
