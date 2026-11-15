package lab.dirtyread;

public class ReadUncommitted {
    public static void main(String[] args) {
        int rounds = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int dirtyReads = 0;
        for (int i = 0; i < rounds; i++) {
            int committedVal = 100;
            int uncommittedVal = 900; // Writer wrote 900, later rolls back to 100!
            int seen = uncommittedVal; // Read Uncommitted reads live buffer directly
            if (seen != committedVal) dirtyReads++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("dirtyReads=" + dirtyReads);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
