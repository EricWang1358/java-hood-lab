package lab.hikarileak;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class LeakDetection {
    public static void main(String[] args) throws Exception {
        int requests = args.length > 0 ? Integer.parseInt(args[0]) : 40;
        Semaphore pool = new Semaphore(10);
        int served = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < requests; i++) {
            if (!pool.tryAcquire(5, TimeUnit.MILLISECONDS)) {
                continue;
            }
            long borrowAt = System.nanoTime();
            try {
                served++;
            } finally {
                long heldMs = (System.nanoTime() - borrowAt) / 1_000_000;
                // 配合 leak-detection-threshold: 2000 监控慢持有并确保 finally 归还
                if (heldMs > 2000) {
                    System.err.println("Apparent connection leak detected");
                }
                pool.release();
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"served\":%d,\"expected\":%d,\"ms\":%d}%n", served, requests, ms);
    }
}
