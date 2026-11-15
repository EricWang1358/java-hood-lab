package lab.backtrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CopyPathBacktrack {
    static int count = 0;

    static void dfs(int[] nums, int start, List<Integer> path) {
        count++;
        for (int i = start; i < nums.length; i++) {
            if (i > start && nums[i] == nums[i - 1]) continue;
            List<Integer> next = new ArrayList<>(path);
            next.add(nums[i]);
            dfs(nums, i + 1, next);
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = i;
        Arrays.sort(nums);
        long start = System.nanoTime();
        count = 0;
        dfs(nums, 0, new ArrayList<>());
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("subsets=" + count);
        System.out.println("expected=" + (1 << n));
        System.out.println("ms=" + ms);
    }
}
