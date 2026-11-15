package lab.sqldeadlock;

import java.util.concurrent.ConcurrentHashMap;

public class InsertIgnoreOrRc {
    public static void main(String[] args) throws Exception {
        int pairs = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int deadlocks = 0;
        ConcurrentHashMap<Integer, String> uniqueIdx = new ConcurrentHashMap<>();
        for (int i = 0; i < pairs; i++) {
            final int key = i;
            Thread t1 = new Thread(() -> uniqueIdx.putIfAbsent(key, "A"));
            Thread t2 = new Thread(() -> uniqueIdx.putIfAbsent(key, "B"));
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("deadlocks=" + deadlocks);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
