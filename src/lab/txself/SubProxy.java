package lab.txself;

import java.util.concurrent.atomic.AtomicInteger;

public class SubProxy {
    static class Db {
        final AtomicInteger stock = new AtomicInteger(100_000);
        final AtomicInteger orders = new AtomicInteger(0);
    }

    static class OrderService {
        protected final Db db;
        protected final ThreadLocal<Boolean> inTx = ThreadLocal.withInitial(() -> false);
        OrderService(Db db) { this.db = db; }

        public void placeOrder(boolean fail) {
            this.payAndRecord(fail);
        }

        public void payAndRecord(boolean fail) {
            int before = db.stock.get();
            db.stock.decrementAndGet();
            if (fail) {
                if (inTx.get()) db.stock.set(before);
                throw new RuntimeException("支付异常");
            }
            db.orders.incrementAndGet();
        }
    }

    // 模拟 Spring CGLIB：委托给原始 target 执行，导致 target 内部的 this 不是代理对象
    static class CglibTargetDelegateProxy extends OrderService {
        private final OrderService target;
        CglibTargetDelegateProxy(OrderService target) {
            super(target.db);
            this.target = target;
        }

        @Override
        public void placeOrder(boolean fail) {
            target.placeOrder(fail);
        }

        @Override
        public void payAndRecord(boolean fail) {
            target.inTx.set(true);
            try {
                target.payAndRecord(fail);
            } finally {
                target.inTx.set(false);
            }
        }
    }

    public static void main(String[] args) {
        int scale = Integer.parseInt(args[0]);
        int total = scale * 1000;
        Db db = new Db();
        OrderService proxy = new CglibTargetDelegateProxy(new OrderService(db));
        int initial = db.stock.get();

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                proxy.placeOrder(true);
            } catch (RuntimeException ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        int dirty = (initial - db.stock.get()) - db.orders.get();
        System.out.println("dirty=" + dirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
