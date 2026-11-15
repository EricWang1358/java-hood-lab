package lab.txthread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncJoinPropagate {
    public static void main(String[] args) throws Exception {
        int total = Integer.parseInt(args[0]);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        AtomicInteger orders = new AtomicInteger(0);
        AtomicInteger items = new AtomicInteger(0);

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            int snap = orders.get();
            try {
                orders.incrementAndGet();
                Future<?> f = pool.submit(() -> {
                    throw new RuntimeException("子任务写明细失败");
                });
                f.get(); // 阻塞等待并把子任务异常抛回主线程触发回滚
                items.incrementAndGet();
            } catch (Exception e) {
                orders.set(snap); // 主事务回滚
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
