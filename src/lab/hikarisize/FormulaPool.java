package lab.hikarisize;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class FormulaPool {
    public static void main(String[] args) throws Exception {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 16;
        int tasks = 160;
        // HikariCP 官方经验公式：connections = core_count * 2 + effective_spindle_count = 4 * 2 + 1 = 9
        Semaphore pool = new Semaphore(9);
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
