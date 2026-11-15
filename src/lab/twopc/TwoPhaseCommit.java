package lab.twopc;

public class TwoPhaseCommit {
    public static void main(String[] args) {
        int crashes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int inconsistentCount = 0;
        for (int i = 0; i < crashes; i++) {
            boolean redoPrepare = true;
            boolean binlogComplete = (i % 2 == 0);
            // Crash recovery checks XID: if binlog is complete -> commit redo; else -> rollback!
            boolean masterHas = redoPrepare && binlogComplete;
            boolean replicaHas = binlogComplete;
            if (masterHas != replicaHas) {
                inconsistentCount++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("inconsistentCount=" + inconsistentCount);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
