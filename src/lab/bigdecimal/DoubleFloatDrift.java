package lab.bigdecimal;

public class DoubleFloatDrift {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int exactMatch = 0;
        for (int i = 0; i < n; i++) {
            double sum = 0.1 + 0.2;
            if (sum == 0.3) exactMatch++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("exactMatch=" + exactMatch);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
