package lab.heapoom;

import java.lang.ref.SoftReference;
import java.util.ArrayDeque;
import java.util.Deque;

public class SoftRefCacheGuard {
    static final int MAX_SOFT_SLOTS = 96;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Deque<SoftReference<byte[]>> q = new ArrayDeque<>(MAX_SOFT_SLOTS);
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            if (q.size() >= MAX_SOFT_SLOTS) {
                q.pollFirst();
            }
            q.addLast(new SoftReference<>(new byte[128 * 1024]));
            ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
