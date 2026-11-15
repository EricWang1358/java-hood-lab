package lab.jvmstack;

public class IterativeLoop {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        long s = 0;
        int reached = 0;
        for (int i = 1; i <= n; i++) {
            s += i;
            reached++;
        }
        if (s < 0) System.out.print("");
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("reached=" + reached);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
