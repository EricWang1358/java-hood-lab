package lab.reflect;

import java.lang.reflect.Method;

public class ReflectLookupEveryTime {
    public static class Target {
        public int add(int x) { return x + 1; }
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        Target t = new Target();
        long start = System.nanoTime();
        int sum = 0;
        for (int i = 0; i < n; i++) {
            Method m = Target.class.getMethod("add", int.class);
            sum += (int) m.invoke(t, 1);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("sum=" + sum);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
