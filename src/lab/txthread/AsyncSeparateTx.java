package lab.txthread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncSeparateTx {
    static final ThreadLocal<Boolean> TX = ThreadLocal.withInitial(() -> false);

    public static void main(String[] args) throws Exception {
        int total = Integer.parseInt(args[0]);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        AtomicInteger orders = new AtomicInteger(0);
        AtomicInteger items = new AtomicInteger(0);

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            TX.set(true);
            try {
                orders.incrementAndGet(); // 主线程先写订单
                pool.submit(() -> {
                    // 子线程拿不到主线程的 ThreadLocal 事务，抛异常也不会回滚主线程
                    if (!TX.get()) throw new RuntimeException("子线程无事务且写明细失败");
                    items.incrementAndGet();
                });
            } finally {
                TX.remove();
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
