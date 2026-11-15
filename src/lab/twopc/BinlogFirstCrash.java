package lab.twopc;

public class BinlogFirstCrash {
    public static void main(String[] args) {
        int crashes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int inconsistentCount = 0;
        for (int i = 0; i < crashes; i++) {
            boolean binlogWritten = true;
            // Crash happens AFTER binlog flush but BEFORE redo commit!
            boolean redoCommitted = false;
            if (redoCommitted != binlogWritten) {
                // Replica replays binlog and gets the row, Master rolls back -> ghost row!
                inconsistentCount++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("inconsistentCount=" + inconsistentCount);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
