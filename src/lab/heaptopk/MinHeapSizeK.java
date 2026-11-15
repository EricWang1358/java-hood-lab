package lab.heaptopk;

import java.util.PriorityQueue;

public class MinHeapSizeK {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int k = 10;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = (i * 31) % n;
        long start = System.nanoTime();
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(k);
        for (int v : arr) {
            if (minHeap.size() < k) {
                minHeap.offer(v);
            } else if (v > minHeap.peek()) {
                minHeap.poll();
                minHeap.offer(v);
            }
        }
        long sum = 0;
        for (int v : minHeap) sum += v;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("topCount=" + (sum > 0 ? minHeap.size() : 0));
        System.out.println("expected=" + k);
        System.out.println("ms=" + ms);
    }
}
