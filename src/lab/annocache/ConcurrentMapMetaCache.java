package lab.annocache;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentMapMetaCache {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Route { String path(); }

    @Route(path = "/orders")
    static class OrderHandler {}

    static final ConcurrentHashMap<Class<?>, String> CACHE = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int matched = 0;
        for (int i = 0; i < n; i++) {
            String path = CACHE.computeIfAbsent(OrderHandler.class, c -> {
                Route r = c.getAnnotation(Route.class);
                return r == null ? "" : r.path();
            });
            if ("/orders".equals(path)) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("matched=" + matched);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
