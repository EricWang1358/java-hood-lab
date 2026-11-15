package lab.hashtwo;

import java.util.HashMap;
import java.util.Map;

public class HashLookup {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = i + 1;
        int target = (n - 1) + n;
        long start = System.nanoTime();
        Map<Integer, Integer> map = new HashMap<>();
        int hit = 0;
        for (int i = 0; i < n; i++) {
            int need = target - nums[i];
            if (map.containsKey(need)) {
                hit = 1;
                break;
            }
            map.put(nums[i], i);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("hit=" + hit);
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
