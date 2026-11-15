package lab.txprop;

import java.util.concurrent.atomic.AtomicInteger;

public class RequiresNew {
    static class TxState {
        int uncommittedOrders = 0;
    }

    static class Db {
        final AtomicInteger orders = new AtomicInteger(0);
    }

    static class TxManager {
        private final Db db;
        private final ThreadLocal<TxState> current = new ThreadLocal<>();
        TxManager(Db db) { this.db = db; }

        void runRequiresNew(Runnable body) {
            TxState suspended = current.get();
            TxState inner = new TxState();
            current.set(inner);
            try {
                body.run();
                db.orders.addAndGet(inner.uncommittedOrders);
            } finally {
                // 恢复被挂起的外层事务
                if (suspended != null) current.set(suspended);
                else current.remove();
            }
        }

        void insertOrder() { current.get().uncommittedOrders++; }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        Db db = new Db();
        TxManager tx = new TxManager(db);

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            tx.runRequiresNew(() -> {
                tx.insertOrder();
                try {
                    tx.runRequiresNew(() -> { throw new RuntimeException("积分服务异常"); });
                } catch (RuntimeException ignored) {
                }
            });
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("main_saved=" + db.orders.get());
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
