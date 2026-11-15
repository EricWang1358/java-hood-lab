package lab.escape;

public class ScalarPrimitives {
    static volatile long sink;

    static int step(int i) {
        int a = i & 15;
        int b = (i + 1) & 15;
        return a + b;
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        long s = 0;
        for (int i = 0; i < n; i++) {
            s += step(i);
        }
        sink = s;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("count=" + n);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
