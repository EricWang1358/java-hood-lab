package lab.mvcthread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncWaitExternal {
    public static void main(String[] args) throws Exception {
        int slowConcurrency = Integer.parseInt(args[0]);
        ExecutorService tomcat = Executors.newFixedThreadPool(4);
        ExecutorService biz = Executors.newFixedThreadPool(4);
        CountDownLatch releaseSlow = new CountDownLatch(1);
        for (int i = 0; i < slowConcurrency; i++) {
            tomcat.submit(() -> {
                // 虽然用了业务池，但 Tomcat 线程同步 .get() 死等，照样占着坑不放！
                try { biz.submit(() -> releaseSlow.await(200, TimeUnit.MILLISECONDS)).get(); }
                catch (Exception ignored) {}
            });
        }
        Thread.sleep(5);
        AtomicInteger healthOk = new AtomicInteger(0);
        long start = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            Future<?> f = tomcat.submit(() -> {});
            try {
                f.get(2, TimeUnit.MILLISECONDS);
                healthOk.incrementAndGet();
            } catch (TimeoutException ignored) {
            }
        }
        releaseSlow.countDown();
        tomcat.shutdown();
        biz.shutdown();
        tomcat.awaitTermination(2, TimeUnit.SECONDS);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("health_ok=" + healthOk.get());
        System.out.println("expected=20");
        System.out.println("ms=" + ms);
    }
}
