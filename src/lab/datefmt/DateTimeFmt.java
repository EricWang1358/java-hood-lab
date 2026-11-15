package lab.datefmt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class DateTimeFmt {
    static final int ROUNDS = 25_000;
    static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern(PATTERN).withZone(ZoneId.systemDefault());
    static final AtomicInteger okFormats = new AtomicInteger();

    static String formatDate(Instant date) {
        return FMT.format(date);
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        Instant[] dates = new Instant[threads];
        String[] want = new String[threads];
        for (int t = 0; t < threads; t++) {
            LocalDateTime ldt = LocalDateTime.of(2020 + t, 1 + t, 10 + t, 8 + t, 15 + t, 20 + t);
            dates[t] = ldt.atZone(ZoneId.systemDefault()).toInstant();
            want[t] = FMT.format(dates[t]);
        }
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            Instant myDate = dates[t];
            String myWant = want[t];
            workers[t] = new Thread(() -> {
                try {
                    barrier.await();
                    int ok = 0;
                    for (int i = 0; i < ROUNDS; i++) {
                        try {
                            if (myWant.equals(formatDate(myDate))) ok++;
                        } catch (Throwable ignored) {
                        }
                    }
                    okFormats.addAndGet(ok);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("okFormats=" + okFormats.get());
        System.out.println("expected=" + (threads * ROUNDS));
        System.out.println("ms=" + ms);
    }
}
