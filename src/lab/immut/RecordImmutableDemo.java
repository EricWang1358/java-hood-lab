package lab.immut;

public class RecordImmutableDemo {
    record ConfigRecord(String app, int maxConn) {}

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int intact = 0;
        for (int i = 0; i < n; i++) {
            ConfigRecord r = new ConfigRecord("order-svc", 64);
            if ("order-svc".equals(r.app()) && r.maxConn() == 64) intact++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("intact=" + intact);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
