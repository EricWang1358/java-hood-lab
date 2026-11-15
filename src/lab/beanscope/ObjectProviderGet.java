package lab.beanscope;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ObjectProviderGet {
    static class ProtoCtx { int userId; }

    static class OrderBean {
        // 正确：注入 ObjectProvider<ProtoCtx>（等价于 Supplier），每次调用现取新实例
        private final Supplier<ProtoCtx> provider;
        OrderBean(Supplier<ProtoCtx> provider) { this.provider = provider; }

        int process(int uid) {
            ProtoCtx ctx = provider.get();
            ctx.userId = uid;
            Thread.yield();
            return ctx.userId;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int perThread = 5000;
        OrderBean singleton = new OrderBean(ProtoCtx::new);
        AtomicInteger corrupted = new AtomicInteger(0);
        Thread[] workers = new Thread[threads];

        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            final int uid = t + 1;
            workers[t] = new Thread(() -> {
                for (int i = 0; i < perThread; i++) {
                    if (singleton.process(uid) != uid) corrupted.incrementAndGet();
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("corrupted=" + corrupted.get());
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
