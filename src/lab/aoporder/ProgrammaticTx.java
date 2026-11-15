package lab.aoporder;

import java.util.concurrent.locks.ReentrantLock;

public class ProgrammaticTx {
    static class Db {
        int committedBalance;
        Db(int init) { this.committedBalance = init; }
    }

    static void txExecute(Db db, Runnable action) {
        int snap = db.committedBalance;
        try {
            action.run();
        } catch (RuntimeException e) {
            db.committedBalance = snap;
            throw e;
        }
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
                    // 无锁预处理在事务/锁外完成，减少持锁与事务时长
                    int delta = 1;
                    lock.lock();
                    try {
                        txExecute(db, () -> db.committedBalance -= delta);
                    } finally {
                        lock.unlock();
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
