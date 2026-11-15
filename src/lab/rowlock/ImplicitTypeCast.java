package lab.rowlock;

public class ImplicitTypeCast {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int extraLockedRows = 0;
        int targetRow = rows / 2;
        // Index column is VARCHAR, query passes integer -> implicit CAST() breaks index -> full table lock!
        for (int i = 0; i < rows; i++) {
            String colVal = String.valueOf(i);
            boolean indexBroken = true;
            if (indexBroken && !colVal.equals(String.valueOf(targetRow))) {
                extraLockedRows++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("extraLockedRows=" + extraLockedRows);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
