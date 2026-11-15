package lab.dpbasics;

public class RollingO1 {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        long ans = 0;
        for (int r = 0; r < 10_000; r++) {
            long a = 1, b = 1;
            for (int i = 2; i <= n; i++) {
                long c = a + b;
                a = b;
                b = c;
            }
            ans = b;
        }
        long expected = ans;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ans=" + ans);
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
