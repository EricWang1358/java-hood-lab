package lab.backtrack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoPruneDup {
    static Set<List<Integer>> seen = new HashSet<>();

    static void dfs(int[] nums, int start, List<Integer> path) {
        seen.add(new ArrayList<>(path));
        for (int i = start; i < nums.length; i++) {
            path.add(nums[i]);
            dfs(nums, i + 1, path);
            path.remove(path.size() - 1);
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = i;
        long start = System.nanoTime();
        seen.clear();
        dfs(nums, 0, new ArrayList<>());
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("subsets=" + seen.size());
        System.out.println("expected=" + (1 << n));
        System.out.println("ms=" + ms);
    }
}
