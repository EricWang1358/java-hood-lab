package lab.cpudiag;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EventWaitNotify {
    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        BlockingQueue<Integer> q = new ArrayBlockingQueue<>(n + 1);
        int[] done = {0};
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < n; i++) {
                    q.take();
                    done[0]++;
                }
            } catch (InterruptedException ignored) {}
        });
        long start = System.nanoTime();
        consumer.start();
        for (int i = 0; i < n; i++) {
            q.put(i);
        }
        consumer.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + done[0]);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
