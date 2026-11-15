package lab.txswallow;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class CheckedExc {
    interface CheckedWork { void run() throws Exception; }

    static class Db {
        final AtomicInteger balance = new AtomicInteger(100_000);
        final AtomicInteger receipts = new AtomicInteger(0);
    }

    static class DefaultTxManager {
        private final Db db;
        DefaultTxManager(Db db) { this.db = db; }

        void runInTx(CheckedWork body) throws Exception {
            int snap = db.balance.get();
            try {
                body.run();
            } catch (RuntimeException | Error e) {
                // Spring 默认规则：只对 RuntimeException 和 Error 回滚
                db.balance.set(snap);
                throw e;
            }
            // Checked Exception (如 IOException) 直接放行提交
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        Db db = new Db();
        DefaultTxManager tx = new DefaultTxManager(db);
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
