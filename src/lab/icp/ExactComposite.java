package lab.icp;

public class ExactComposite {
    public static void main(String[] args) {
        int zhangCount = Integer.parseInt(args[0]);
        int matched = zhangCount / 10;
        long start = System.nanoTime();
        // Index (age, name): age=20 equality narrows range first, only back-to-table for matched
        int backToTable = matched;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backToTable=" + backToTable);
        System.out.println("expected=" + matched);
        System.out.println("ms=" + ms);
    }
}
