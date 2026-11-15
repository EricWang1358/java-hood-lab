package lab.tryres;

public class TryWithResourcesSafe {
    static class Resource implements AutoCloseable {
        boolean closed = false;
        void work() { throw new RuntimeException("primary work fail"); }
        @Override
        public void close() {
            closed = true;
            throw new RuntimeException("close fail");
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int primaryPreserved = 0;
        for (int i = 0; i < n; i++) {
            Resource holder = null;
            try (Resource r = new Resource()) {
                holder = r;
                r.work();
            } catch (RuntimeException e) {
                if ("primary work fail".equals(e.getMessage())
                        && e.getSuppressed().length == 1
                        && holder != null && holder.closed) {
                    primaryPreserved++;
                }
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("closedOk=" + primaryPreserved);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
