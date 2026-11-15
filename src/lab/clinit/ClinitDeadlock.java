package lab.clinit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClinitDeadlock {
    static class A {
        static {
            try { Thread.sleep(15); } catch (InterruptedException ignored) {}
            B.ping();
        }
        static void ping() {}
    }

    static class B {
        static {
            try { Thread.sleep(15); } catch (InterruptedException ignored) {}
            A.ping();
        }
        static void ping() {}
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        CountDownLatch done = new CountDownLatch(2);
        Thread t1 = new Thread(() -> { A.ping(); done.countDown(); });
        Thread t2 = new Thread(() -> { B.ping(); done.countDown(); });
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        boolean finished = done.await(60, TimeUnit.MILLISECONDS);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + (finished ? n : 0));
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
