package lab.spiloader;

import java.util.ServiceLoader;

public class RealServiceLoaderEmpty {
    public interface Codec { int encode(int x); }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            ServiceLoader<Codec> loader = ServiceLoader.load(Codec.class);
            if (!loader.iterator().hasNext()) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
