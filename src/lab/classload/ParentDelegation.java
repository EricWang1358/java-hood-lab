package lab.classload;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class ParentDelegation {
    public static class PluginPayload {
        public static int ver = 1;
    }

    static byte[] readClassBytes(String name) throws Exception {
        String path = "/" + name.replace('.', '/') + ".class";
        try (InputStream in = ParentDelegation.class.getResourceAsStream(path)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
            return bos.toByteArray();
        }
    }

    static class StandardLoader extends ClassLoader {
        private final byte[] bytes;
        StandardLoader(ClassLoader parent, byte[] bytes) {
            super(parent);
            this.bytes = bytes;
        }
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.endsWith("PluginPayload")) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
        }
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        String target = PluginPayload.class.getName();
        byte[] bytes = readClassBytes(target);
        Set<Class<?>> classes = new HashSet<>();

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            ClassLoader cl = new StandardLoader(ParentDelegation.class.getClassLoader(), bytes);
            classes.add(cl.loadClass(target));
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("distinctClasses=" + classes.size());
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
