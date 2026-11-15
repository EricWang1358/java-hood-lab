package lab.proxykind;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

public class JdkCastImpl {
    interface OrderService { void pay(); }
    static class OrderServiceImpl implements OrderService {
        @Override public void pay() {}
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 10_000;
        AtomicInteger intercepted = new AtomicInteger(0);
        OrderService target = new OrderServiceImpl();
        Object proxy = Proxy.newProxyInstance(
            OrderService.class.getClassLoader(),
            new Class<?>[]{OrderService.class},
            (p, m, a) -> { intercepted.incrementAndGet(); return m.invoke(target, a); }
        );

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                // 错误：把 JDK 动态代理强转成实现类 OrderServiceImpl
                OrderServiceImpl impl = (OrderServiceImpl) proxy;
                impl.pay();
            } catch (ClassCastException ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("intercepted=" + intercepted.get());
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
