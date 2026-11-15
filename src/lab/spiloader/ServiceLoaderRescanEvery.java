package lab.spiloader;

public class ServiceLoaderRescanEvery {
    interface Codec { int encode(int x); }
    public static class JsonCodec implements Codec {
        public int encode(int x) { return x + 1; }
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        String fqcn = JsonCodec.class.getName();
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            Codec c = (Codec) Class.forName(fqcn).getDeclaredConstructor().newInstance();
            if (c.encode(1) == 2) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
