package lab.varargs;

public class StaticDispatchByRef {
    static class Base {}
    static class Sub extends Base {}

    static int tag(Base b) { return 1; }
    static int tag(Sub s) { return 2; }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Base ref = new Sub();
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            if (tag(ref) == 1) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
