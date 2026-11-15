package lab.icp;

public class WithIcp {
    public static void main(String[] args) {
        int zhangCount = Integer.parseInt(args[0]);
        int[] ages = new int[zhangCount];
        for (int i = 0; i < zhangCount; i++) ages[i] = 18 + (i % 10);

        long start = System.nanoTime();
        // With ICP: storage engine filters age=20 inside index(name, age) BEFORE back-to-table!
        int backToTable = 0;
        int matched = 0;
        for (int i = 0; i < zhangCount; i++) {
            if (ages[i] == 20) {
                backToTable++;
                matched++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backToTable=" + backToTable);
        System.out.println("expected=" + matched);
        System.out.println("ms=" + ms);
    }
}
