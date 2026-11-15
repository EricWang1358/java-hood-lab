package lab.references;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

public class PhantomCleanup {
    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        ReferenceQueue<byte[]> queue = new ReferenceQueue<>();
        List<PhantomReference<byte[]>> refs = new ArrayList<>(n);

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            byte[] payload = new byte[4096];
            refs.add(new PhantomReference<>(payload, queue));
        }
        System.gc();
        Thread.sleep(15);

        int reclaimed = 0;
        Reference<? extends byte[]> r;
        while ((r = queue.poll()) != null) {
            r.clear();
            reclaimed++;
        }
        refs.clear();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("reclaimed=" + reclaimed);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
