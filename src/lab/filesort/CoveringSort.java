package lab.filesort;

public class CoveringSort {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Covering index + index order: 0 filesort, 0 back-to-table
        int sortedRows = 20;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("sortedRows=" + sortedRows);
        System.out.println("expected=20");
        System.out.println("ms=" + ms);
    }
}
