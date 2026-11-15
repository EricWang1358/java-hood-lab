package lab.hikarileak;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SwallowTimeout {
    public static void main(String[] args) throws Exception {
        int requests = args.length > 0 ? Integer.parseInt(args[0]) : 40;
        Semaphore pool = new Semaphore(10);
        int served = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < requests; i++) {
            if (!pool.tryAcquire(5, TimeUnit.MILLISECONDS)) {
                continue;
            }
            try {
                if (i % 3 == 0) {
                    throw new RuntimeException("SQL constraint error");
                }
                served++;
                pool.release(); // 只在正常路径还连接，异常路径泄漏！
            } catch (RuntimeException e) {
                // 吞掉异常直接返回，既没记日志也没在 finally 里 release()
            }
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"served\":%d,\"expected\":%d,\"ms\":%d}%n", served, requests, ms);
    }
}
