package lab.rowlock;

public class NoIndexFullScan {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        String[] orderNo = new String[rows];
        boolean[] rowLocked = new boolean[rows];
        for (int i = 0; i < rows; i++) orderNo[i] = "ORD-" + i;

        String target = "ORD-" + (rows / 2);
        // No index on orderNo -> InnoDB scans every clustered row and acquires lock!
        for (int i = 0; i < rows; i++) {
            rowLocked[i] = true; // Lock acquired on scanned clustered record
            if (orderNo[i].equals(target)) {
                // Even after finding target, scan continues to end of table!
            }
        }
        int extraLockedRows = 0;
        for (int i = 0; i < rows; i++) {
            if (rowLocked[i] && !orderNo[i].equals(target)) extraLockedRows++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("extraLockedRows=" + extraLockedRows);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
