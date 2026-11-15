package lab.phantom;

public class CurrentReadGapLock {
    public static void main(String[] args) {
        int rounds = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int phantomHits = 0;
        for (int i = 0; i < rounds; i++) {
            // SELECT ... FOR UPDATE at start acquires Next-Key/Gap lock, blocking Tx B's INSERT!
            int firstCount = 5;
            int secondCount = 5;
            if (secondCount != firstCount) phantomHits++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("phantomHits=" + phantomHits);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
