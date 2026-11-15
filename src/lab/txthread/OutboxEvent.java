package lab.txthread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class OutboxEvent {
    public static void main(String[] args) throws Exception {
        int total = Integer.parseInt(args[0]);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        AtomicInteger orders = new AtomicInteger(0);
        AtomicInteger items = new AtomicInteger(0);

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            // 主事务内只同步落库订单与本地事件表，不跨线程共享事务
            orders.incrementAndGet();
            // 提交后由异步任务/补偿调度保证最终一致
            pool.submit(items::incrementAndGet);
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
