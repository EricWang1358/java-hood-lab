package lab.aslist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CopyToNewArrayList {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            String[] arr = new String[]{"a", "b"};
            List<String> list = new ArrayList<>(Arrays.asList(arr));
            list.add("c");
            list.set(0, "Z");
            if (list.size() == 3 && "a".equals(arr[0])) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
