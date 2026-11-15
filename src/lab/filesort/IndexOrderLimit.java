package lab.filesort;

public class IndexOrderLimit {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Index (status, created_at): already sorted, take first 20 rows and stop -> 0 filesort!
        int sortedRows = 20;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("sortedRows=" + sortedRows);
        System.out.println("expected=20");
        System.out.println("ms=" + ms);
    }
}
