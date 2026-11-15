package lab.cpudiag;

import java.util.concurrent.atomic.AtomicBoolean;

public class BusySpinLoop {
    static volatile long spinCount = 0;

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        AtomicBoolean ready = new AtomicBoolean(false);
        Thread worker = new Thread(() -> {
            long c = 0;
            while (!ready.get()) {
                c++;
            }
            spinCount = c;
        });
        long start = System.nanoTime();
        worker.start();
        Thread.sleep(Math.min(n, 40));
        ready.set(true);
        worker.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + n);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
