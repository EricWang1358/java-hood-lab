package lab.intcache;

public class NewIntegerBypass {
    @SuppressWarnings({"removal", "deprecation", "NumberEquality"})
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int correct = 0;
        for (int i = 0; i < n; i++) {
            Integer a = new Integer(i);
            Integer b = new Integer(i);
            if (a == b) correct++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("correct=" + correct);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
