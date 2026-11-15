package lab.txself;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

public class SplitService {
    interface PaymentService {
        void payAndRecord(boolean fail);
    }

    static class Db {
        final AtomicInteger stock = new AtomicInteger(100_000);
        final AtomicInteger orders = new AtomicInteger(0);
    }

    static class PaymentServiceImpl implements PaymentService {
        private final Db db;
        final ThreadLocal<Boolean> inTx = ThreadLocal.withInitial(() -> false);
        PaymentServiceImpl(Db db) { this.db = db; }

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

    static class OrderService {
        private final PaymentService paymentService;
        OrderService(PaymentService paymentService) { this.paymentService = paymentService; }

        public void placeOrder(boolean fail) {
            paymentService.payAndRecord(fail);
        }
    }

    static PaymentService wrapTxProxy(PaymentServiceImpl target) {
        return (PaymentService) Proxy.newProxyInstance(
            PaymentService.class.getClassLoader(),
            new Class<?>[]{PaymentService.class},
            (proxy, method, args) -> {
                target.inTx.set(true);
                try {
                    return method.invoke(target, args);
                } finally {
                    target.inTx.set(false);
                }
            }
        );
    }

    public static void main(String[] args) {
        int scale = Integer.parseInt(args[0]);
        int total = scale * 1000;
        Db db = new Db();
        PaymentService paymentProxy = wrapTxProxy(new PaymentServiceImpl(db));
        OrderService orderService = new OrderService(paymentProxy);
        int initial = db.stock.get();

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                orderService.placeOrder(true);
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
