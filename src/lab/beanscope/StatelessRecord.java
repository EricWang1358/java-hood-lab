package lab.beanscope;

import java.util.concurrent.atomic.AtomicInteger;

public class StatelessRecord {
    static class OrderBean {
        int process(int uid) {
            int localUserId = uid;
            Thread.yield();
            return localUserId;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int perThread = 5000;
        OrderBean singleton = new OrderBean();
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
