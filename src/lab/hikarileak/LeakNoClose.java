package lab.hikarileak;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class LeakNoClose {
    public static void main(String[] args) throws Exception {
        int requests = args.length > 0 ? Integer.parseInt(args[0]) : 40;
        Semaphore pool = new Semaphore(10);
        int served = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < requests; i++) {
            // connectionTimeout = 5ms
            if (!pool.tryAcquire(5, TimeUnit.MILLISECONDS)) {
                continue; // HikariPool-1 - Connection is not available, request timed out!
            }
            // 忘记在 finally / try-with-resources 里调用 conn.close()，连接永久泄漏！
            if (i % 2 == 0) {
                served++;
                continue; // 提前 return，没还连接
            }
            served++;
            // 即便这里没提前 return 也忘了 close()
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"served\":%d,\"expected\":%d,\"ms\":%d}%n", served, requests, ms);
    }
}
