package lab.aoporder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class UnorderedLockInsideTx {
    static class Db {
        volatile int committedBalance;
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
                    // 错误顺序：外层是事务（读已提交快照），内层才是锁
                    int txLocal;
                    lock.lock();
                    try {
                        txLocal = db.committedBalance - 1;
                    } finally {
                        lock.unlock(); // 锁先释放了，事务还没提交！
                    }
                    Thread.yield();
                    db.committedBalance = txLocal; // 事务提交覆盖别人
                }
            });
            workers[i].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        int lost = db.committedBalance; // 预期应扣到 0
        System.out.println("lost=" + lost);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
