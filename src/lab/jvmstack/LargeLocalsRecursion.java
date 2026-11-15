package lab.jvmstack;

public class LargeLocalsRecursion {
    static long sum(int n, int[] maxDepth, int cur) {
        long a = n + 1L, b = n + 2L, c = n + 3L, d = n + 4L;
        long e = n + 5L, f = n + 6L, g = n + 7L, h = n + 8L;
        long i = n + 9L, j = n + 10L, k = n + 11L, l = n + 12L;
        if (cur > maxDepth[0]) maxDepth[0] = cur;
        if (n <= 0) return (a + b + c + d + e + f + g + h + i + j + k + l) & 1L;
        return ((a ^ b ^ c ^ d ^ e ^ f ^ g ^ h ^ i ^ j ^ k ^ l) & 1L)
                + sum(n - 1, maxDepth, cur + 1);
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] maxDepth = {0};
        long start = System.nanoTime();
        try {
            sum(n, maxDepth, 0);
        } catch (StackOverflowError ignored) {
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("reached=" + Math.min(maxDepth[0], n));
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
