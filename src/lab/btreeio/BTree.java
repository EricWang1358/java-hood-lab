package lab.btreeio;

public class BTree {
    public static void main(String[] args) {
        int range = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // B-Tree stores row payload in internal nodes -> lower fanout (~16) -> height ~5, range needs tree backtrack
        int pageReads = 5 + Math.max(1, range / 16);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("pageReads=" + pageReads);
        System.out.println("expected=" + (3 + Math.max(1, range / 100)));
        System.out.println("ms=" + ms);
    }
}
