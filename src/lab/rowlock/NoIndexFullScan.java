package lab.rowlock;

public class NoIndexFullScan {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int extraLockedRows = 0;
        int targetRow = rows / 2;
        // No index on WHERE column -> InnoDB scans all clustered rows and locks every scanned row!
        for (int i = 0; i < rows; i++) {
            boolean locked = true;
            if (locked && i != targetRow) {
                extraLockedRows++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("extraLockedRows=" + extraLockedRows);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
