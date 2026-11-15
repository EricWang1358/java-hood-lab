package lab.escape;

public class EscapingToArray {
    static class Pair {
        final int a, b;
        Pair(int a, int b) { this.a = a; this.b = b; }
    }
    static volatile long sink;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Pair[] ring = new Pair[4096];
        long start = System.nanoTime();
        long s = 0;
        for (int i = 0; i < n; i++) {
            Pair p = new Pair(i & 15, (i + 1) & 15);
            ring[i & 4095] = p;
            s += p.a + p.b;
        }
        sink = s + ring[0].a;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("count=" + n);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
