package lab.jvmstack;

public class DeepRecursion {
    static long sum(int n, int[] maxDepth, int cur) {
        if (cur > maxDepth[0]) maxDepth[0] = cur;
        if (n <= 0) return 0;
        return n + sum(n - 1, maxDepth, cur + 1);
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
