package lab.enumstate;

import java.util.EnumMap;
import java.util.Map;

public class EnumMapArrayFast {
    enum OrderState { NEW, PAID, SHIPPED, DONE }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Map<OrderState, String> labels = new EnumMap<>(OrderState.class);
        labels.put(OrderState.NEW, "新建");
        labels.put(OrderState.PAID, "已支付");
        labels.put(OrderState.SHIPPED, "已发货");
        labels.put(OrderState.DONE, "已完成");
        OrderState[] vals = OrderState.values();
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            if (labels.get(vals[i & 3]) != null) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
