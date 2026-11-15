package lab.icp;

public class NoIcpFilter {
    public static void main(String[] args) {
        int zhangCount = Integer.parseInt(args[0]);
        int[] ages = new int[zhangCount];
        for (int i = 0; i < zhangCount; i++) ages[i] = 18 + (i % 10);

        long start = System.nanoTime();
        // Without ICP: every '张%' row is fetched back-to-table BEFORE checking age=20!
        int backToTable = 0;
        int matched = 0;
        for (int i = 0; i < zhangCount; i++) {
            backToTable++;
            if (ages[i] == 20) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("backToTable=" + backToTable);
        System.out.println("expected=" + matched);
        System.out.println("ms=" + ms);
    }
}
