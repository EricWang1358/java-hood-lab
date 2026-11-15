package lab.enumstate;

import java.util.HashMap;
import java.util.Map;

public class MagicIntHashMap {
    static final int NEW = 0, PAID = 1, SHIPPED = 2, DONE = 3;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Map<Integer, String> labels = new HashMap<>();
        labels.put(NEW, "新建");
        labels.put(PAID, "已支付");
        labels.put(SHIPPED, "已发货");
        labels.put(DONE, "已完成");
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            if (labels.get(i & 3) != null) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
