package lab.twopc;

public class SyncBinlogGroupCommit {
    public static void main(String[] args) {
        int crashes = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int inconsistentCount = 0;
        int groupSize = 16;
        for (int i = 0; i < crashes; i += groupSize) {
            int batch = Math.min(groupSize, crashes - i);
            for (int j = 0; j < batch; j++) {
                boolean binlogComplete = ((i + j) % 2 == 0);
                boolean masterHas = binlogComplete;
                boolean replicaHas = binlogComplete;
                if (masterHas != replicaHas) inconsistentCount++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("inconsistentCount=" + inconsistentCount);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
