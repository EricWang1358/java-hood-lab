package lab.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandleInvoke {
    public static class Target {
        public int add(int x) { return x + 1; }
    }

    public static void main(String[] args) throws Throwable {
        int n = Integer.parseInt(args[0]);
        Target t = new Target();
        MethodHandle mh = MethodHandles.lookup()
                .findVirtual(Target.class, "add", MethodType.methodType(int.class, int.class));
        long start = System.nanoTime();
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += (int) mh.invokeExact(t, 1);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("sum=" + sum);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
