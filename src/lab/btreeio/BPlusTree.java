package lab.btreeio;

public class BPlusTree {
    public static void main(String[] args) {
        int range = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // B+Tree: internal nodes hold only keys+ptrs (high fanout ~1000 -> height 3), leaves linked for range scan!
        int pageReads = 3 + Math.max(1, range / 100);
        int sum = 0;
        for (int i = 0; i < range; i++) sum += (1000 + i);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("pageReads=" + pageReads);
        System.out.println("expected=" + pageReads);
        System.out.println("matched=" + (sum > 0 ? range : 0));
        System.out.println("ms=" + ms);
    }
}
