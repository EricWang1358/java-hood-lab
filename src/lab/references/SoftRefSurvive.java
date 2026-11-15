package lab.references;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class SoftRefSurvive {
    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        List<SoftReference<byte[]>> cache = new ArrayList<>(n);

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            cache.add(new SoftReference<>(new byte[4096]));
        }
        System.gc();
        Thread.sleep(10);
        int reclaimed = 0;
        for (SoftReference<byte[]> s : cache) {
            if (s.get() == null) reclaimed++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("reclaimed=" + reclaimed);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
