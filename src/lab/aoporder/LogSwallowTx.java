package lab.aoporder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class LogSwallowTx {
    static class Db {
        int committedBalance;
        Db(int init) { this.committedBalance = init; }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int perThread = 2000;
        Db db = new Db(threads * perThread);
        ReentrantLock lock = new ReentrantLock();
        AtomicInteger dirty = new AtomicInteger(0);
        Thread[] workers = new Thread[threads];

        long start = System.nanoTime();
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int n = 0; n < perThread; n++) {
                    lock.lock();
                    try {
                        int snap = db.committedBalance;
                        try {
                            // 内层日志切面：吞掉异常，导致外层事务切面以为成功而提交
                            try {
                                db.committedBalance = snap - 1;
                                if (n % 2 == 0) throw new RuntimeException("风控拦截");
                            } catch (RuntimeException logSwallowed) {
                                // 仅打印日志，忘了 rethrow
                                dirty.incrementAndGet();
                            }
                        } catch (RuntimeException txRollback) {
                            db.committedBalance = snap;
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            });
            workers[i].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lost=" + dirty.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
