package lab.dpbasics;

public class PlainRecursion {
    static long fib(int n) {
        if (n <= 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        long ans = fib(n);
        long expected = fibFast(n);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ans=" + ans);
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }

    static long fibFast(int n) {
        long a = 1, b = 1;
        for (int i = 2; i <= n; i++) { long c = a + b; a = b; b = c; }
        return b;
    }
}
