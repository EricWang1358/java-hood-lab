package lab.dpbasics;

public class Tabulation1D {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        long ans = 0;
        for (int r = 0; r < 10_000; r++) {
            long[] dp = new long[n + 1];
            dp[0] = 1;
            dp[1] = 1;
            for (int i = 2; i <= n; i++) dp[i] = dp[i - 1] + dp[i - 2];
            ans = dp[n];
        }
        long expected = ans;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ans=" + ans);
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
