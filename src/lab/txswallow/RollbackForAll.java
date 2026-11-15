package lab.txswallow;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class RollbackForAll {
    interface CheckedWork { void run() throws Exception; }

    static class Db {
        final AtomicInteger balance = new AtomicInteger(100_000);
        final AtomicInteger receipts = new AtomicInteger(0);
    }

    static class RollbackForTxManager {
        private final Db db;
        private final Class<? extends Throwable> rollbackFor;
        RollbackForTxManager(Db db, Class<? extends Throwable> rollbackFor) {
            this.db = db;
            this.rollbackFor = rollbackFor;
        }

        void runInTx(CheckedWork body) throws Exception {
            int snap = db.balance.get();
            try {
                body.run();
            } catch (Throwable e) {
                if (rollbackFor.isInstance(e) || e instanceof Error) {
                    db.balance.set(snap);
                }
                throw e;
            }
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        Db db = new Db();
        RollbackForTxManager tx = new RollbackForTxManager(db, Exception.class);
        int initial = db.balance.get();

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                tx.runInTx(() -> {
                    db.balance.decrementAndGet();
                    throw new IOException("磁盘写入失败");
                });
            } catch (Exception ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        int dirty = (initial - db.balance.get()) - db.receipts.get();
        System.out.println("dirty=" + dirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
