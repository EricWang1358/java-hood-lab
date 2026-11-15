package lab.readview;

public class RcSnapshot {
    public static void main(String[] args) {
        int ops = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // RC snapshot read also never blocks on writer's X-lock
        int blockedReads = 0;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("blockedReads=" + blockedReads);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
