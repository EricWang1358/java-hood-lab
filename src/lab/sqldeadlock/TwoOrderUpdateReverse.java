package lab.sqldeadlock;

public class TwoOrderUpdateReverse {
    public static void main(String[] args) {
        int pairs = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int deadlocks = 0;
        for (int i = 0; i < pairs; i++) {
            // Tx1 locks row 1 -> waits row 2; Tx2 locks row 2 -> waits row 1
            boolean tx1WantsRow2 = true;
            boolean tx2WantsRow1 = true;
            if (tx1WantsRow2 && tx2WantsRow1) {
                deadlocks++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("deadlocks=" + deadlocks);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
