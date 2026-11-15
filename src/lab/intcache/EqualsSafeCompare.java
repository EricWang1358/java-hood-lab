package lab.intcache;

import java.util.Objects;

public class EqualsSafeCompare {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int correct = 0;
        for (int i = 0; i < n; i++) {
            Integer a = Integer.valueOf(i);
            Integer b = Integer.valueOf(i);
            if (Objects.equals(a, b)) correct++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("correct=" + correct);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
