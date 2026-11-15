package lab.annocache;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ClassValueMetaCache {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Route { String path(); }

    @Route(path = "/orders")
    static class OrderHandler {}

    static final ClassValue<String> ROUTE_VALUE = new ClassValue<>() {
        @Override
        protected String computeValue(Class<?> type) {
            Route r = type.getAnnotation(Route.class);
            return r == null ? "" : r.path();
        }
    };

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int matched = 0;
        for (int i = 0; i < n; i++) {
            if ("/orders".equals(ROUTE_VALUE.get(OrderHandler.class))) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("matched=" + matched);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
