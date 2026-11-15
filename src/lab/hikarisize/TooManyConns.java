package lab.hikarisize;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class TooManyConns {
    public static void main(String[] args) throws Exception {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 16;
        int tasks = 160;
        // 误区：把 maximum-pool-size 设到 200，结果大量连接同时涌入 4 核数据库，引发锁竞争与上下文切换雪崩
        Semaphore pool = new Semaphore(200);
        Semaphore dbCores = new Semaphore(4);
        AtomicInteger activeInDb = new AtomicInteger();
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(tasks);
        AtomicInteger completed = new AtomicInteger();

        long t0 = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            exec.submit(() -> {
                try {
                    pool.acquire();
                    int active = activeInDb.incrementAndGet();
                    try {
                        // 超过数据库核心数的活跃连接会造成额外锁争用与调度抖动
                        if (active > 4) {
                            Thread.sleep(Math.min(18, (active - 4) * 2L));
                        }
                        dbCores.acquire();
                        try {
                            Thread.sleep(1);
                            completed.incrementAndGet();
                        } finally {
                            dbCores.release();
                        }
                    } finally {
                        activeInDb.decrementAndGet();
                        pool.release();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        exec.shutdown();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        System.out.printf("{\"completed\":%d,\"expected\":%d,\"ms\":%d}%n", completed.get(), tasks, ms);
    }
}
