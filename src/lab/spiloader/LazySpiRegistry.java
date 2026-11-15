package lab.spiloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class LazySpiRegistry {
    interface Codec { int encode(int x); }
    static class JsonCodec implements Codec { public int encode(int x) { return x + 1; } }

    static final Map<String, Supplier<Codec>> REGISTRY = new ConcurrentHashMap<>();
    static { REGISTRY.put("json", JsonCodec::new); }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            Codec c = REGISTRY.get("json").get();
            if (c.encode(1) == 2) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
