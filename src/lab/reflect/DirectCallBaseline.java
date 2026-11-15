package lab.reflect;

public class DirectCallBaseline {
    public static class Target {
        public int add(int x) { return x + 1; }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Target t = new Target();
        long start = System.nanoTime();
        int sum = 0;
        for (int i = 0; i < n; i++) {
            sum += t.add(1);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("sum=" + sum);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
