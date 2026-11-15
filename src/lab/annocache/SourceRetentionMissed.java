package lab.annocache;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SourceRetentionMissed {
    @Retention(RetentionPolicy.SOURCE)
    @interface Route { String path(); }

    @Route(path = "/orders")
    static class OrderHandler {}

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int matched = 0;
        for (int i = 0; i < n; i++) {
            Route r = OrderHandler.class.getAnnotation(Route.class);
            if (r != null && "/orders".equals(r.path())) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("matched=" + matched);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
