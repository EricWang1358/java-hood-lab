package lab.proxykind;

import java.util.concurrent.atomic.AtomicInteger;

public class CglibOpen {
    static class OrderService {
        public void pay() {}
    }

    static class CglibSubclassProxy extends OrderService {
        private final OrderService target;
        private final AtomicInteger intercepted;
        CglibSubclassProxy(OrderService target, AtomicInteger intercepted) {
            this.target = target;
            this.intercepted = intercepted;
        }

        @Override
        public void pay() {
            intercepted.incrementAndGet();
            target.pay();
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 10_000;
        AtomicInteger intercepted = new AtomicInteger(0);
        OrderService proxy = new CglibSubclassProxy(new OrderService(), intercepted);

        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            proxy.pay();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("intercepted=" + intercepted.get());
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
