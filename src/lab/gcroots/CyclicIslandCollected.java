package lab.gcroots;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CyclicIslandCollected {
    static class Node { Node peer; byte[] data = new byte[1024]; }

    static void allocateIslands(int n, List<WeakReference<Node>> probes) {
        for (int i = 0; i < n; i++) {
            Node a = new Node();
            Node b = new Node();
            a.peer = b;
            b.peer = a;
            probes.add(new WeakReference<>(a));
        }
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        List<WeakReference<Node>> probes = new ArrayList<>(n);

        long start = System.nanoTime();
        allocateIslands(n, probes);
        System.gc();
        Thread.sleep(10);
        int reclaimed = 0;
        for (WeakReference<Node> p : probes) {
            if (p.get() == null) reclaimed++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("reclaimed=" + reclaimed);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
