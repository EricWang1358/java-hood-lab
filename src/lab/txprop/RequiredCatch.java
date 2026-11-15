package lab.txprop;

import java.util.concurrent.atomic.AtomicInteger;

public class RequiredCatch {
    static class TxState {
        int uncommittedOrders = 0;
        boolean rollbackOnly = false;
    }

    static class Db {
        final AtomicInteger orders = new AtomicInteger(0);
    }

    static class TxManager {
        private final Db db;
        private final ThreadLocal<TxState> current = new ThreadLocal<>();
        TxManager(Db db) { this.db = db; }

        void runRequired(Runnable body) {
            boolean isNew = current.get() == null;
            if (isNew) current.set(new TxState());
            TxState tx = current.get();
            try {
                body.run();
                if (isNew) {
                    if (tx.rollbackOnly) {
                        throw new RuntimeException("UnexpectedRollbackException");
                    }
                    db.orders.addAndGet(tx.uncommittedOrders);
                }
            } catch (RuntimeException e) {
                tx.rollbackOnly = true;
                throw e;
            } finally {
                if (isNew) current.remove();
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
            try {
                tx.runRequired(() -> {
                    tx.insertOrder();
                    try {
                        tx.runRequired(() -> { throw new RuntimeException("积分服务异常"); });
                    } catch (RuntimeException ignored) {
                        // 外层吃掉异常，以为能保住主订单
                    }
                });
            } catch (RuntimeException ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("main_saved=" + db.orders.get());
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
