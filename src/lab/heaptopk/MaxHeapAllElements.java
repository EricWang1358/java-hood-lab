package lab.heaptopk;

import java.util.Comparator;
import java.util.PriorityQueue;

public class MaxHeapAllElements {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int k = 10;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = (i * 31) % n;
        long start = System.nanoTime();
        PriorityQueue<Integer> pq = new PriorityQueue<>(n, Comparator.reverseOrder());
        for (int v : arr) pq.offer(v);
        long sum = 0;
        for (int i = 0; i < k; i++) sum += pq.poll();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("topCount=" + (sum > 0 ? k : 0));
        System.out.println("expected=" + k);
        System.out.println("ms=" + ms);
    }
}
