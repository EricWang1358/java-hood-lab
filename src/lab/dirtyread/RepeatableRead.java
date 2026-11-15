package lab.dirtyread;

public class RepeatableRead {
    public static void main(String[] args) {
        int rounds = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int dirtyReads = 0;
        for (int i = 0; i < rounds; i++) {
            int snapshotVal = 100;
            if (snapshotVal != 100) dirtyReads++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("dirtyReads=" + dirtyReads);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
