package lab.twopc;

public class RedoFirstCrash {
    public static void main(String[] args) {
        int crashes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int inconsistentCount = 0;
        for (int i = 0; i < crashes; i++) {
            boolean redoCommitted = true;
            // Crash happens AFTER redo commit but BEFORE binlog flush!
            boolean binlogWritten = false;
            if (redoCommitted != binlogWritten) {
                // Master has the row, Replica never receives it via binlog -> split-brain!
                inconsistentCount++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("inconsistentCount=" + inconsistentCount);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
