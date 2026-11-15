package lab.ioccycle;

import java.util.HashSet;
import java.util.Set;

public class ConstructorCycle {
    static class A { final B b; A(B b) { this.b = b; } }
    static class B { final A a; B(A a) { this.a = a; } }

    static A createA(Set<String> creating) {
        if (!creating.add("a")) throw new IllegalStateException("BeanCurrentlyInCreationException");
        B b = createB(creating);
        return new A(b);
    }

    static B createB(Set<String> creating) {
        if (!creating.add("b")) throw new IllegalStateException("BeanCurrentlyInCreationException");
        A a = createA(creating);
        return new B(a);
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                createA(new HashSet<>());
                ok++;
            } catch (IllegalStateException ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("proxy_injected=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
