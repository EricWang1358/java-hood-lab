package lab.clinit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SafeLazyHolder {
    static class GoodConfig {
        static final int BASE = 100;
        final int effective;
        private GoodConfig() { this.effective = BASE + 1; }
        private static class Holder {
            static final GoodConfig INSTANCE = new GoodConfig();
        }
        static GoodConfig getInstance() {
            return Holder.INSTANCE;
        }
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        AtomicInteger ok = new AtomicInteger();

        for (int i = 0; i < n; i++) {
            Thread t = new Thread(() -> {
                try { ready.await(); } catch (InterruptedException ignored) {}
                if (GoodConfig.getInstance().effective == 101) {
                    ok.incrementAndGet();
                }
                done.countDown();
            });
            t.start();
        }
        ready.countDown();
        done.await();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok.get());
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
