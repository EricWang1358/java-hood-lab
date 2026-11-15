package lab.stackqueue;

import java.util.TreeMap;

public class TreeMapWin {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int k = 500;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = (i * 37) % 10000;
        long start = System.nanoTime();
        TreeMap<Integer, Integer> map = new TreeMap<>();
        long checksum = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            map.merge(nums[i], 1, Integer::sum);
            if (i >= k) {
                int out = nums[i - k];
                int c = map.get(out);
                if (c == 1) map.remove(out);
                else map.put(out, c - 1);
            }
            if (i >= k - 1) {
                checksum += map.lastKey();
                count++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("windows=" + count);
        System.out.println("expected=" + (n - k + 1));
        System.out.println("checksum=" + checksum);
        System.out.println("ms=" + ms);
    }
}
