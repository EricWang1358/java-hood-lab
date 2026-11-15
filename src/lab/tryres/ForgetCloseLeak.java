package lab.tryres;

public class ForgetCloseLeak {
    static class Resource {
        boolean closed = false;
        void work() { throw new RuntimeException("work fail"); }
        void close() { closed = true; }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int closedOk = 0;
        for (int i = 0; i < n; i++) {
            Resource r = new Resource();
            try {
                r.work();
                r.close();
            } catch (RuntimeException ignored) {
            }
            if (r.closed) closedOk++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("closedOk=" + closedOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
