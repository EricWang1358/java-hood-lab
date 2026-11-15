package lab.txprop;

import java.util.concurrent.atomic.AtomicInteger;

public class NestedSavepoint {
    static class TxState {
        int uncommittedOrders = 0;
        int uncommittedPoints = 0;
    }

    static class Db {
        final AtomicInteger orders = new AtomicInteger(0);
    }

    static class TxManager {
        private final Db db;
        private final ThreadLocal<TxState> current = new ThreadLocal<>();
        TxManager(Db db) { this.db = db; }

        void runRequired(Runnable body) {
            TxState tx = new TxState();
            current.set(tx);
            try {
                body.run();
                db.orders.addAndGet(tx.uncommittedOrders);
            } finally {
                current.remove();
            }
        }

        void runNested(Runnable body) {
            TxState tx = current.get();
            int saveOrders = tx.uncommittedOrders;
            int savePoints = tx.uncommittedPoints;
            try {
                body.run();
            } catch (RuntimeException e) {
                // 回滚到 Savepoint，不影响外层状态
                tx.uncommittedOrders = saveOrders;
                tx.uncommittedPoints = savePoints;
                throw e;
            }
        }

        void insertOrder() { current.get().uncommittedOrders++; }
        void addPoints() { current.get().uncommittedPoints++; }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        Db db = new Db();
        TxManager tx = new TxManager(db);

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            tx.runRequired(() -> {
                tx.insertOrder();
                try {
                    tx.runNested(() -> {
                        tx.addPoints();
                        throw new RuntimeException("积分服务异常");
                    });
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
