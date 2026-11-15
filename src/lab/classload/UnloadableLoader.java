package lab.classload;

public class UnloadableLoader {
    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        String target = ParentDelegation.PluginPayload.class.getName();
        byte[] bytes = ParentDelegation.readClassBytes(target);
        int distinct = 0;
        int prevHash = 0;

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            ClassLoader cl = new ChildFirstLoader.ChildLoader(
                    UnloadableLoader.class.getClassLoader(), target, bytes);
            Class<?> c = cl.loadClass(target);
            int h = System.identityHashCode(c);
            if (h != prevHash) {
                distinct++;
                prevHash = h;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("distinctClasses=" + distinct);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
