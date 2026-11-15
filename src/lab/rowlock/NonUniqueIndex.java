package lab.rowlock;

public class NonUniqueIndex {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Secondary non-unique index hits 1 row, plus 1 next-key gap lock on the neighbor
        int extraLockedRows = (rows > 1) ? 1 : 0;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("extraLockedRows=" + extraLockedRows);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
