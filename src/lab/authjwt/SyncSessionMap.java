package lab.authjwt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncSessionMap {
    static final Map<String, String> SESSIONS = new HashMap<>();

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int perThread = 50_000;
        for (int i = 0; i < 1000; i++) {
            SESSIONS.put("sid-" + i, "user-" + i);
        }

        AtomicInteger ok = new AtomicInteger();
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                int localOk = 0;
                for (int i = 0; i < perThread; i++) {
                    String sid = "sid-" + (i % 1000);
                    synchronized (SESSIONS) {
                        if (SESSIONS.get(sid) != null) localOk++;
                    }
                }
                ok.addAndGet(localOk);
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok.get());
        System.out.println("expected=" + (threads * perThread));
        System.out.println("ms=" + ms);
    }
}
