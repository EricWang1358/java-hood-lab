package lab.tryres;

public class FinallyCloseSwallow {
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
            Resource r = new Resource();
            try {
                try {
                    r.work();
                } finally {
                    r.close();
                }
            } catch (RuntimeException e) {
                if ("primary work fail".equals(e.getMessage()) && r.closed) {
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
