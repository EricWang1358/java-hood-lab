package lab.hikarileak;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TryWithResources {
    static class PooledConn implements AutoCloseable {
        private final Semaphore pool;
        PooledConn(Semaphore pool) { this.pool = pool; }
        @Override public void close() { pool.release(); }
    }

    public static void main(String[] args) throws Exception {
        int requests = args.length > 0 ? Integer.parseInt(args[0]) : 40;
        Semaphore pool = new Semaphore(10);
        int served = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < requests; i++) {
            if (!pool.tryAcquire(5, TimeUnit.MILLISECONDS)) {
                continue;
            }
            // try-with-resources 确保无论正常返回还是抛异常都必定归还连接到 HikariCP
            try (PooledConn conn = new PooledConn(pool)) {
                served++;
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"served\":%d,\"expected\":%d,\"ms\":%d}%n", served, requests, ms);
    }
}
