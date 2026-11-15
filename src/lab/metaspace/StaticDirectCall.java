package lab.metaspace;

import java.util.HashSet;
import java.util.Set;

public class StaticDirectCall {
    static class DirectService implements NewProxyClassEvery.Service {
        @Override public int ping() { return 1; }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Set<Class<?>> classes = new HashSet<>();

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            NewProxyClassEvery.Service s = new DirectService();
            classes.add(s.getClass());
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("classesCreated=" + classes.size());
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
