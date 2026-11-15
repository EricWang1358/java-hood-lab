package lab.metaspace;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

public class NewProxyClassEvery {
    public interface Service { int ping(); }

    static class IsolatedLoader extends ClassLoader {
        IsolatedLoader(ClassLoader parent) { super(parent); }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Set<Class<?>> proxyClasses = new HashSet<>();

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            ClassLoader cl = new IsolatedLoader(Service.class.getClassLoader());
            Service s = (Service) Proxy.newProxyInstance(
                    cl, new Class<?>[]{Service.class}, (p, m, a) -> 1);
            proxyClasses.add(s.getClass());
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("classesCreated=" + proxyClasses.size());
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
