package lab.mvcthread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BulkheadLimit {
    public static void main(String[] args) throws Exception {
        int slowConcurrency = Integer.parseInt(args[0]);
        ExecutorService tomcat = Executors.newFixedThreadPool(4);
        Semaphore bulkhead = new Semaphore(2); // 舱壁隔离：慢接口最多同时占 2 个并发，超了直接快速失败
        CountDownLatch releaseSlow = new CountDownLatch(1);
        for (int i = 0; i < slowConcurrency; i++) {
            tomcat.submit(() -> {
                if (bulkhead.tryAcquire()) {
                    try { releaseSlow.await(200, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) {}
                    finally { bulkhead.release(); }
                }
            });
        }
        Thread.sleep(5);
        AtomicInteger healthOk = new AtomicInteger(0);
        long start = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            Future<?> f = tomcat.submit(() -> {});
            try {
                f.get(10, TimeUnit.MILLISECONDS);
                healthOk.incrementAndGet();
            } catch (TimeoutException ignored) {
            }
        }
        releaseSlow.countDown();
        tomcat.shutdown();
        tomcat.awaitTermination(2, TimeUnit.SECONDS);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("health_ok=" + healthOk.get());
        System.out.println("expected=20");
        System.out.println("ms=" + ms);
    }
}
