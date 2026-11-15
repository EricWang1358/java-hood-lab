package lab.stackqueue;

public class WindowScan {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int k = 500;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) nums[i] = (i * 37) % 10000;
        long start = System.nanoTime();
        long checksum = 0;
        for (int i = 0; i <= n - k; i++) {
            int max = nums[i];
            for (int j = i + 1; j < i + k; j++) {
                if (nums[j] > max) max = nums[j];
            }
            checksum += max;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("windows=" + (n - k + 1));
        System.out.println("expected=" + (n - k + 1));
        System.out.println("checksum=" + checksum);
        System.out.println("ms=" + ms);
    }
}
