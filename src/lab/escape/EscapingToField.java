package lab.escape;

public class EscapingToField {
    static class Pair {
        final int a, b;
        Pair(int a, int b) { this.a = a; this.b = b; }
    }
    static volatile Pair escaped;
    static volatile long sink;

    static int step(int i) {
        Pair p = new Pair(i & 15, (i + 1) & 15);
        escaped = p;
        return p.a + p.b;
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
