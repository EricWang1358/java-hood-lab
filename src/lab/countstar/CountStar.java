package lab.countstar;

public class CountStar {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        byte[] smallestIndexLeaf = new byte[rows];

        long start = System.nanoTime();
        int counted = 0;
        // COUNT(*): MySQL special-cases it; does NOT expand columns, same tree scan as COUNT(1)
        for (int i = 0; i < smallestIndexLeaf.length; i++) {
            counted++;
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("counted=" + counted);
        System.out.println("expected=" + rows);
        System.out.println("ms=" + ms);
    }
}
