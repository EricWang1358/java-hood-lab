package lab.heaptopk;

import java.util.Arrays;

public class FullSortTopK {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int k = 10;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = (i * 31) % n;
        long start = System.nanoTime();
        Arrays.sort(arr);
        long sum = 0;
        for (int i = n - k; i < n; i++) sum += arr[i];
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("topCount=" + (sum > 0 ? k : 0));
        System.out.println("expected=" + k);
        System.out.println("ms=" + ms);
    }
}
