package lab.gcroots;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ExplicitNullRelease {
    static class Node { Node peer; byte[] data = new byte[1024]; }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        List<Node> tempBatch = new ArrayList<>(n);
        List<WeakReference<Node>> probes = new ArrayList<>(n);

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            Node a = new Node();
            Node b = new Node();
            a.peer = b;
            b.peer = a;
            tempBatch.add(a);
            probes.add(new WeakReference<>(a));
        }
        tempBatch = null;
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
