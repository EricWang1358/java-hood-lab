package lab.listadd;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Queue {
    static final int ROUNDS = 100_000;
    static ConcurrentLinkedQueue<String> logs;

    static void append(String entry) {
        logs.add(entry);
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int expected = threads * ROUNDS;
        logs = new ConcurrentLinkedQueue<>();
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                for (int i = 0; i < ROUNDS; i++) {
                    append("ok");
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        int valid = 0;
        for (String s : logs) {
            if (s != null) valid++;
        }
        System.out.println("valid=" + valid);
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
