package lab.hikarisize;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class TinyPool {
    public static void main(String[] args) throws Exception {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 16;
        int tasks = 160;
        // 连接池过小（仅 2 个连接），远低于数据库 4 核并发能力，大量线程干等连接
        Semaphore pool = new Semaphore(2);
        Semaphore dbCores = new Semaphore(4);
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(tasks);
        AtomicInteger completed = new AtomicInteger();

        long t0 = System.nanoTime();
        for (int i = 0; i < tasks; i++) {
            exec.submit(() -> {
                try {
                    pool.acquire();
                    try {
                        dbCores.acquire();
                        try {
                            Thread.sleep(1);
                            completed.incrementAndGet();
                        } finally {
                            dbCores.release();
                        }
                    } finally {
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
