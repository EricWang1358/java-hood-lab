package lab.classload;

import java.util.HashSet;
import java.util.Set;

public class ContextClassLoaderSpi {
    static Class<?> loadViaTccl(String name) throws ClassNotFoundException {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return Class.forName(name, false, tccl);
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        String target = ParentDelegation.PluginPayload.class.getName();
        byte[] bytes = ParentDelegation.readClassBytes(target);
        Set<Class<?>> classes = new HashSet<>();

        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        long start = System.nanoTime();
        try {
            for (int i = 0; i < n; i++) {
                ClassLoader pluginCl = new ChildFirstLoader.ChildLoader(orig, target, bytes);
                Thread.currentThread().setContextClassLoader(pluginCl);
                classes.add(loadViaTccl(target));
            }
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("distinctClasses=" + classes.size());
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
