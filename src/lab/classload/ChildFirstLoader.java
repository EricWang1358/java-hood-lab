package lab.classload;

import java.util.HashSet;
import java.util.Set;

public class ChildFirstLoader {
    static class ChildLoader extends ClassLoader {
        private final String target;
        private final byte[] bytes;

        ChildLoader(ClassLoader parent, String target, byte[] bytes) {
            super(parent);
            this.target = target;
            this.bytes = bytes;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.equals(target)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    c = defineClass(name, bytes, 0, bytes.length);
                }
                if (resolve) resolveClass(c);
                return c;
            }
            return super.loadClass(name, resolve);
        }
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        String target = ParentDelegation.PluginPayload.class.getName();
        byte[] bytes = ParentDelegation.readClassBytes(target);
        Set<Class<?>> classes = new HashSet<>();

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            ClassLoader cl = new ChildLoader(ChildFirstLoader.class.getClassLoader(), target, bytes);
            classes.add(cl.loadClass(target));
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("distinctClasses=" + classes.size());
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
