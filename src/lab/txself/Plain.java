package lab.txself;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

public class Plain {
    interface OrderService {
        void placeOrder(boolean fail);
        void payAndRecord(boolean fail);
    }

    static class Db {
        final AtomicInteger stock = new AtomicInteger(100_000);
        final AtomicInteger orders = new AtomicInteger(0);
    }

    static class OrderServiceImpl implements OrderService {
        private final Db db;
        private final ThreadLocal<Boolean> inTx = ThreadLocal.withInitial(() -> false);
        OrderServiceImpl(Db db) { this.db = db; }

        @Override
        public void placeOrder(boolean fail) {
            this.payAndRecord(fail);
        }

        @Override
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

    static OrderService wrapTxProxy(OrderService target, ThreadLocal<Boolean> inTx) {
        return (OrderService) Proxy.newProxyInstance(
            OrderService.class.getClassLoader(),
            new Class<?>[]{OrderService.class},
            (proxy, method, args) -> {
                if (!method.getName().equals("payAndRecord")) {
                    return method.invoke(target, args);
                }
                inTx.set(true);
                try {
                    return method.invoke(target, args);
                } finally {
                    inTx.set(false);
                }
            }
        );
    }

    public static void main(String[] args) {
        int scale = Integer.parseInt(args[0]);
        int total = scale * 1000;
        Db db = new Db();
        OrderServiceImpl raw = new OrderServiceImpl(db);
        OrderService proxy = wrapTxProxy(raw, raw.inTx);
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
