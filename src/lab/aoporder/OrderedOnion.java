package lab.aoporder;

import java.util.concurrent.locks.ReentrantLock;

public class OrderedOnion {
    static class Db {
        int committedBalance;
        Db(int init) { this.committedBalance = init; }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int perThread = 2000;
        Db db = new Db(threads * perThread);
        ReentrantLock lock = new ReentrantLock();
        Thread[] workers = new Thread[threads];

        long start = System.nanoTime();
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int n = 0; n < perThread; n++) {
                    // 正确洋葱顺序：@Order(1) 锁在外层，@Order(2) 事务在内层
                    lock.lock();
                    try {
                        int snap = db.committedBalance;
                        try {
                            int txLocal = snap - 1;
                            Thread.yield();
                            db.committedBalance = txLocal; // 先提交事务
                        } catch (RuntimeException e) {
                            db.committedBalance = snap;
                            throw e;
                        }
                    } finally {
                        lock.unlock(); // 事务提交后才释放锁
                    }
                }
            });
            workers[i].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lost=" + db.committedBalance);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
