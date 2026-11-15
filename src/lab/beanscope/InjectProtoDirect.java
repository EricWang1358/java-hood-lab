package lab.beanscope;

import java.util.concurrent.atomic.AtomicInteger;

public class InjectProtoDirect {
    static class ProtoCtx { int userId; }

    static class OrderBean {
        // 错误：单例启动时只注入了一次 prototype 实例，后续永远共用同一份
        private final ProtoCtx ctx;
        OrderBean(ProtoCtx ctx) { this.ctx = ctx; }

        int process(int uid) {
            ctx.userId = uid;
            Thread.yield();
            return ctx.userId;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int perThread = 5000;
        OrderBean singleton = new OrderBean(new ProtoCtx());
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
