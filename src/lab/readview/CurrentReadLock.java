package lab.readview;

public class CurrentReadLock {
    public static void main(String[] args) {
        int ops = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // SELECT ... FOR UPDATE / UPDATE is current read, NOT snapshot read -> blocks on X-lock!
        int blockedReads = ops;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("blockedReads=" + blockedReads);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
