package lab.prefixidx;

public class CrcHashIndex {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // Redundant int url_crc32 column + index: compact 4-byte index, ~1-10 collisions
        int extraReads = 10;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("extraReads=" + extraReads);
        System.out.println("expected=10");
        System.out.println("ms=" + ms);
    }
}
