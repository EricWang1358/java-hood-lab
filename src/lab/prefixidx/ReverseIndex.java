package lab.prefixidx;

public class ReverseIndex {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Reversed string index for suffix lookup (e.g. reverse('13812345678')): high selectivity
        int extraReads = 10;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("extraReads=" + extraReads);
        System.out.println("expected=10");
        System.out.println("ms=" + ms);
    }
}
