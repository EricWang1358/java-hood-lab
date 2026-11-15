package lab.binsearch;

public class WrongLoopBug {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = i / 2;
        int target = (n * 3 / 4) / 2;
        int expected = target * 2;
        long start = System.nanoTime();
        int ans = -1;
        for (int q = 0; q < 200; q++) {
            int l = 0, r = n - 1;
            int steps = 0;
            while (l < r && steps++ < 64) {
                int mid = (l + r) / 2;
                if (nums[mid] == target) { ans = mid; break; }
                else if (nums[mid] < target) l = mid + 1;
                else r = mid - 1;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("idx=" + ans);
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
