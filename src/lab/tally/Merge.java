package lab.tally;

import java.util.concurrent.ConcurrentHashMap;

public class Merge {
    static final String[] ITEMS = {"apple", "milk", "bread", "egg", "rice", "tea", "soap", "juice"};
    static final ConcurrentHashMap<String, Integer> sales = new ConcurrentHashMap<>();
    static final int ROUNDS = 200_000;

    static void addSale(String item) {
        sales.merge(item, 1, Integer::sum);
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        for (String item : ITEMS) sales.put(item, 0);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            int offset = t;
            workers[t] = new Thread(() -> {
                for (int i = 0; i < ROUNDS; i++) {
                    addSale(ITEMS[(i + offset) % ITEMS.length]);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        int total = 0;
        for (int count : sales.values()) total += count;
        System.out.println("total=" + total);
        System.out.println("expected=" + (threads * ROUNDS));
        System.out.println("ms=" + ms);
    }
}
