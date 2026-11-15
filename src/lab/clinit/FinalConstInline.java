package lab.clinit;

public class FinalConstInline {
    static class LazyConst {
        static final int BASE = 100;
        static int clinitRan = 0;
        static {
            clinitRan = 1;
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            int x = LazyConst.BASE + 1;
            if (x == 101) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
