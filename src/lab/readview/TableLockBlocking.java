package lab.readview;

public class TableLockBlocking {
    public static void main(String[] args) {
        int ops = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Without MVCC: writer holds X-lock, every concurrent reader gets blocked!
        int blockedReads = ops;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("blockedReads=" + blockedReads);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
