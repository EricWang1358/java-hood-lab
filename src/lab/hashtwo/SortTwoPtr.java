package lab.hashtwo;

import java.util.Arrays;

public class SortTwoPtr {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = n - i;
        int target = (n - 1) + n;
        long start = System.nanoTime();
        Arrays.sort(nums);
        int l = 0, r = n - 1, hit = 0;
        while (l < r) {
            int sum = nums[l] + nums[r];
            if (sum == target) { hit = 1; break; }
            if (sum < target) l++;
            else r--;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("hit=" + hit);
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
