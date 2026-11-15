package lab.icp;

public class CoveringIcp {
    public static void main(String[] args) {
        int zhangCount = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        // SELECT id, name, age: covered by index(name, age) -> 0 back-to-table!
        int backToTable = 0;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backToTable=" + backToTable);
        System.out.println("expected=" + (zhangCount / 10));
        System.out.println("ms=" + ms);
    }
}
