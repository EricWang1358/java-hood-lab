package lab.tryres;

public class MultiResourceOrder {
    static class Resource implements AutoCloseable {
        final StringBuilder log;
        final String name;
        Resource(String name, StringBuilder log) { this.name = name; this.log = log; }
        @Override
        public void close() { log.append(name); }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int closedOk = 0;
        for (int i = 0; i < n; i++) {
            StringBuilder log = new StringBuilder(2);
            try (Resource r1 = new Resource("1", log);
                 Resource r2 = new Resource("2", log)) {
                // work
            }
            if ("21".equals(log.toString())) closedOk++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("closedOk=" + closedOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
