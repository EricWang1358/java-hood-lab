package lab.rowlock;

public class UniqueIndexPoint {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int targetRow = rows / 2;
        // Primary/Unique index exact hit -> degrades to single Record Lock!
        int lockedRow = targetRow;
        int extraLockedRows = (lockedRow == targetRow) ? 0 : 1;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("extraLockedRows=" + extraLockedRows);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
