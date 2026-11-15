package lab.sqlrank;

public class NestedSubquery {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] dept = new int[n];
        int[] sal = new int[n];
        for (int i = 0; i < n; i++) {
            dept[i] = i % 50;
            sal[i] = (i * 17) % 1000;
        }
        long start = System.nanoTime();
        int top3Count = 0;
        for (int i = 0; i < n; i++) {
            int higher = 0;
            for (int j = 0; j < n; j++) {
                if (dept[j] == dept[i] && sal[j] > sal[i]) higher++;
            }
            if (higher < 3) top3Count++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("top3=" + top3Count);
        System.out.println("expected=150");
        System.out.println("ms=" + ms);
    }
}
