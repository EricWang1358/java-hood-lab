package lab.mvcthread;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedTomcatPool {
    public static void main(String[] args) throws Exception {
        int slowConcurrency = Integer.parseInt(args[0]);
        // 模拟 Tomcat 仅有 4 个工作线程，全部被慢接口占满
        ExecutorService tomcat = Executors.newFixedThreadPool(4);
        CountDownLatch slowStarted = new CountDownLatch(Math.min(4, slowConcurrency));
        CountDownLatch releaseSlow = new CountDownLatch(1);
        for (int i = 0; i < slowConcurrency; i++) {
            tomcat.submit(() -> {
                slowStarted.countDown();
                try { releaseSlow.await(200, TimeUnit.MILLISECONDS); } catch (InterruptedException ignored) {}
            });
        }
        slowStarted.await(50, TimeUnit.MILLISECONDS);
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
        tomcat.awaitTermination(2, TimeUnit.SECONDS);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("health_ok=" + healthOk.get());
        System.out.println("expected=20");
        System.out.println("ms=" + ms);
    }
}
