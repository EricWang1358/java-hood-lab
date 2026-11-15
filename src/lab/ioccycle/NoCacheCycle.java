package lab.ioccycle;

public class NoCacheCycle {
    static class A { B b; }
    static class B { A a; }

    static A createA(int depth) {
        if (depth > 100) throw new StackOverflowError("循环创建死循环");
        A a = new A();
        a.b = createB(depth + 1);
        return a;
    }

    static B createB(int depth) {
        B b = new B();
        b.a = createA(depth + 1);
        return b;
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                createA(0);
                ok++;
            } catch (StackOverflowError ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("proxy_injected=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
