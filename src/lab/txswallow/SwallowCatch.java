package lab.txswallow;

import java.util.concurrent.atomic.AtomicInteger;

public class SwallowCatch {
    static class Db {
        final AtomicInteger balance = new AtomicInteger(100_000);
        final AtomicInteger receipts = new AtomicInteger(0);
    }

    static class TxManager {
        private final Db db;
        TxManager(Db db) { this.db = db; }

        void runInTx(Runnable body) {
            int snap = db.balance.get();
            try {
                body.run();
                // 没收到异常，代理默认 commit
            } catch (RuntimeException e) {
                db.balance.set(snap);
            }
        }
    }

    static void transferAndSwallow(Db db) {
        try {
            db.balance.decrementAndGet();
            throw new RuntimeException("写流水超时");
        } catch (RuntimeException ignored) {
            // 自己把异常吞了，只打日志不抛出
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        Db db = new Db();
        TxManager tx = new TxManager(db);
        int initial = db.balance.get();

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            tx.runInTx(() -> transferAndSwallow(db));
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        int dirty = (initial - db.balance.get()) - db.receipts.get();
        System.out.println("dirty=" + dirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
