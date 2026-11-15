package lab.dpbasics;

public class MemoTopDown {
    static long fib(int n, long[] memo) {
        if (n <= 1) return 1;
        if (memo[n] != 0) return memo[n];
        return memo[n] = fib(n - 1, memo) + fib(n - 2, memo);
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        long ans = 0;
        for (int r = 0; r < 10_000; r++) {
            ans = fib(n, new long[n + 1]);
        }
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
