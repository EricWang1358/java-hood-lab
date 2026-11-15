package lab.stackqueue;

import java.util.ArrayDeque;
import java.util.Deque;

public class ArrayDequeMono {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int k = 500;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = (i * 37) % 10000;
        long start = System.nanoTime();
        Deque<Integer> q = new ArrayDeque<>(k);
        long checksum = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            while (!q.isEmpty() && nums[q.peekLast()] <= nums[i]) q.pollLast();
            q.offerLast(i);
            if (q.peekFirst() <= i - k) q.pollFirst();
            if (i >= k - 1) {
                checksum += nums[q.peekFirst()];
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
