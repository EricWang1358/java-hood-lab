package lab.listadd;

import java.util.ArrayList;
import java.util.List;

public class VolatileList {
    static final int ROUNDS = 100_000;
    static volatile List<String> logs;

    static void append(String entry) {
        logs.add(entry);
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int expected = threads * ROUNDS;
        logs = new ArrayList<>(expected + 64);
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
        int n = logs.size();
        for (int i = 0; i < n; i++) {
            try {
                if (logs.get(i) != null) valid++;
            } catch (Exception ignored) {
            }
        }
        System.out.println("valid=" + valid);
        System.out.println("expected=" + expected);
        System.out.println("ms=" + ms);
    }
}
