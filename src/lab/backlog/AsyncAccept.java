package lab.backlog;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncAccept {
    public static void main(String[] args) throws Exception {
        int clients = Integer.parseInt(args[0]);
        AtomicInteger connected = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try (ServerSocket server = new ServerSocket(0, 128)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                for (int i = 0; i < clients; i++) {
                    try {
                        Socket s = server.accept();
                        pool.submit(() -> {
                            try (s) {
                                s.getInputStream().read();
                            } catch (Exception ignored) {
                            }
                        });
                    } catch (Exception e) {
                        break;
                    }
                }
            });
            srv.start();

            CountDownLatch startGate = new CountDownLatch(1);
            Thread[] workers = new Thread[clients];
            for (int i = 0; i < clients; i++) {
                workers[i] = new Thread(() -> {
                    try {
                        startGate.await();
                        try (Socket c = new Socket()) {
                            c.connect(new InetSocketAddress("127.0.0.1", port), 40);
                            c.getOutputStream().write(1);
                            connected.incrementAndGet();
                        }
                    } catch (Exception ignored) {
                    }
                });
                workers[i].start();
            }
            long start = System.nanoTime();
            startGate.countDown();
            for (Thread w : workers) w.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            srv.join();
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
            System.out.println("connected=" + connected.get());
            System.out.println("expected=" + clients);
            System.out.println("ms=" + ms);
        }
    }
}
