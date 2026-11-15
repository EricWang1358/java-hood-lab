package lab.binsearch;

public class HalfOpenBound {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = i / 2;
        int target = (n * 3 / 4) / 2;
        int expected = target * 2;
        long start = System.nanoTime();
        int ans = -1;
        for (int q = 0; q < 200; q++) {
            int l = 0, r = n;
            while (l < r) {
                int mid = l + ((r - l) >>> 1);
                if (nums[mid] >= target) r = mid;
                else l = mid + 1;
            }
            ans = l;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("idx=" + ans);
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
