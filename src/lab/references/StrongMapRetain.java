package lab.references;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrongMapRetain {
    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        Map<Integer, byte[]> cache = new HashMap<>();
        List<WeakReference<byte[]>> probes = new ArrayList<>(n);

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            byte[] payload = new byte[4096];
            cache.put(i, payload);
            probes.add(new WeakReference<>(payload));
        }
        System.gc();
        Thread.sleep(10);
        int reclaimed = 0;
        for (WeakReference<byte[]> p : probes) {
            if (p.get() == null) reclaimed++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("reclaimed=" + reclaimed);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
