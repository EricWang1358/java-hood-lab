package lab.heapoom;

import java.util.HashMap;
import java.util.Map;

public class UnboundedCacheLeak {
    static final long BUDGET_BYTES = 16L * 1024 * 1024;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Map<Integer, byte[]> leakMap = new HashMap<>();
        long retained = 0;
        int ok = 0;

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            byte[] payload = new byte[128 * 1024];
            if (retained + payload.length > BUDGET_BYTES) {
                break; // OutOfMemoryError: Java heap space
            }
            leakMap.put(i, payload);
            retained += payload.length;
            ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
