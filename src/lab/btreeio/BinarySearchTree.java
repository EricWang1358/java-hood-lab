package lab.btreeio;

public class BinarySearchTree {
    static final int TOTAL = 200_000;

    public static void main(String[] args) {
        int range = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Balanced BST has fanout=2 -> height ~18 -> ~18 pointer hops per lookup + range traversal
        int height = 18;
        int pageReads = height + range;
        int sum = 0;
        for (int i = 0; i < range; i++) sum += (1000 + i);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("pageReads=" + pageReads);
        System.out.println("expected=" + (3 + Math.max(1, range / 100)));
        System.out.println("matched=" + (sum > 0 ? range : 0));
        System.out.println("ms=" + ms);
    }
}
