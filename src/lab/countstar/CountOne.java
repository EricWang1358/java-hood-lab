package lab.countstar;

public class CountOne {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        byte[] smallestIndexLeaf = new byte[rows]; // represents smallest secondary index entries

        long start = System.nanoTime();
        int counted = 0;
        // COUNT(1): InnoDB walks smallest index tree without extracting row columns
        for (int i = 0; i < smallestIndexLeaf.length; i++) {
            counted++;
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("counted=" + counted);
        System.out.println("expected=" + rows);
        System.out.println("ms=" + ms);
    }
}
