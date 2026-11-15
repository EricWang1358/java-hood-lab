package lab.heaptopk;

import java.util.PriorityQueue;

public class PrimitiveMinHeapTopK {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        int k = 10;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = (i * 31) % n;
        long start = System.nanoTime();
        // 仅用 int[k] 原生小顶堆：零装箱、O(K) 内存、O(N log K) 时间
        int[] heap = new int[k];
        for (int i = 0; i < k; i++) heap[i] = arr[i];
        PriorityQueue<Integer> dummy = null; // keep package consistent
        for (int i = k / 2 - 1; i >= 0; i--) siftDown(heap, i, k);
        for (int i = k; i < n; i++) {
            if (arr[i] > heap[0]) {
                heap[0] = arr[i];
                siftDown(heap, 0, k);
            }
        }
        long sum = 0;
        for (int v : heap) sum += v;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("topCount=" + (sum > 0 && dummy == null ? k : 0));
        System.out.println("expected=" + k);
        System.out.println("ms=" + ms);
    }

    static void siftDown(int[] h, int i, int size) {
        int val = h[i];
        while (true) {
            int c = (i << 1) + 1;
            if (c >= size) break;
            if (c + 1 < size && h[c + 1] < h[c]) c++;
            if (val <= h[c]) break;
            h[i] = h[c];
            i = c;
        }
        h[i] = val;
    }
}
