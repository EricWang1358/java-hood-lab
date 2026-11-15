package lab.stackqueue;

public class PrimitiveMono {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int k = 500;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = (i * 37) % 10000;
        long start = System.nanoTime();
        int[] q = new int[n];
        int head = 0, tail = 0;
        long checksum = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            while (head < tail && nums[q[tail - 1]] <= nums[i]) tail--;
            q[tail++] = i;
            if (q[head] <= i - k) head++;
            if (i >= k - 1) {
                checksum += nums[q[head]];
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
