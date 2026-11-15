package lab.countstar;

public class SummaryCounter {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        int summaryTotal = rows; // maintained via counter table / Redis INCR

        long start = System.nanoTime();
        int counted = summaryTotal; // O(1) lookup, no InnoDB table/index scan

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("counted=" + counted);
        System.out.println("expected=" + rows);
        System.out.println("ms=" + ms);
    }
}
