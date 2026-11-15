package lab.txprop;

import java.util.concurrent.atomic.AtomicInteger;

public class AsyncOutbox {
    static class Db {
        final AtomicInteger orders = new AtomicInteger(0);
    }

    static class TxManager {
        private final Db db;
        TxManager(Db db) { this.db = db; }

        void runTx(Runnable body, Runnable afterCommit) {
            int before = db.orders.get();
            try {
                body.run();
            } catch (RuntimeException e) {
                db.orders.set(before);
                throw e;
            }
            // 主事务提交后，再发事件/异步处理非核心逻辑，不占双倍连接
            afterCommit.run();
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        Db db = new Db();
        TxManager tx = new TxManager(db);

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            tx.runTx(
                db.orders::incrementAndGet,
                () -> {
                    try {
                        throw new RuntimeException("积分服务异常");
                    } catch (RuntimeException ignored) {
                    }
                }
            );
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("main_saved=" + db.orders.get());
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
