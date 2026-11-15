package lab.hashtwo;

public class BruteForce {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = i + 1;
        int target = (n - 1) + n;
        long start = System.nanoTime();
        int hit = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (nums[i] + nums[j] == target) {
                    hit = 1;
                    break;
                }
            }
            if (hit == 1) break;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("hit=" + hit);
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
