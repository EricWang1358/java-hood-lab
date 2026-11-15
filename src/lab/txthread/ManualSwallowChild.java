package lab.txthread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ManualSwallowChild {
    public static void main(String[] args) throws Exception {
        int total = Integer.parseInt(args[0]);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        AtomicInteger orders = new AtomicInteger(0);
        AtomicInteger items = new AtomicInteger(0);

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            orders.incrementAndGet();
            Future<?> f = pool.submit(() -> {
                throw new RuntimeException("子任务写明细失败");
            });
            try {
                f.get();
                items.incrementAndGet();
            } catch (Exception ignored) {
                // 捕获 Future 异常却未上抛，主事务照常提交
            }
        }
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        long ms = (System.nanoTime() - start) / 1_000_000;
        int dirty = orders.get() - items.get();
        System.out.println("dirty=" + dirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
