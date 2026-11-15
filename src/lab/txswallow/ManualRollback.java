package lab.txswallow;

import java.util.concurrent.atomic.AtomicInteger;

public class ManualRollback {
    static class Db {
        final AtomicInteger balance = new AtomicInteger(100_000);
        final AtomicInteger receipts = new AtomicInteger(0);
    }

    static class TxManager {
        private final Db db;
        final ThreadLocal<Boolean> rollbackOnly = ThreadLocal.withInitial(() -> false);
        TxManager(Db db) { this.db = db; }

        void setRollbackOnly() { rollbackOnly.set(true); }

        void runInTx(Runnable body) {
            int snap = db.balance.get();
            rollbackOnly.set(false);
            try {
                body.run();
                if (rollbackOnly.get()) {
                    db.balance.set(snap);
                }
            } catch (RuntimeException e) {
                db.balance.set(snap);
            } finally {
                rollbackOnly.remove();
            }
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        Db db = new Db();
        TxManager tx = new TxManager(db);
        int initial = db.balance.get();

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            tx.runInTx(() -> {
                try {
                    db.balance.decrementAndGet();
                    throw new RuntimeException("写流水超时");
                } catch (RuntimeException e) {
                    // 吞掉异常返回兜底值，但显式标红事务回滚
                    tx.setRollbackOnly();
                }
            });
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        int dirty = (initial - db.balance.get()) - db.receipts.get();
        System.out.println("dirty=" + dirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
