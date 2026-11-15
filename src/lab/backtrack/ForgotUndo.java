package lab.backtrack;

import java.util.ArrayList;
import java.util.List;

public class ForgotUndo {
    static int validSum = 0;

    static void dfs(int[] nums, int start, List<Integer> path) {
        if (path.size() <= nums.length) validSum += path.size();
        for (int i = start; i < nums.length; i++) {
            path.add(nums[i]);
            dfs(nums, i + 1, path);
            // Bug: forgot path.remove(path.size() - 1)
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = i;
        long start = System.nanoTime();
        validSum = 0;
        dfs(nums, 0, new ArrayList<>());
        int expected = n * (1 << (n - 1));
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("subsets=" + validSum);
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
