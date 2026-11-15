package lab.biooom;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedPoolStarve {
    public static void main(String[] args) throws Exception {
        int clients = Integer.parseInt(args[0]);
        int slowCount = Math.min(4, clients / 2);
        int fastCount = clients - slowCount;
        AtomicInteger fastOk = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(4);

        try (ServerSocket server = new ServerSocket(0, 256)) {
            int port = server.getLocalPort();
            Thread acceptor = new Thread(() -> {
                for (int i = 0; i < clients; i++) {
                    try {
                        Socket s = server.accept();
                        pool.submit(() -> {
                            try (s; InputStream in = s.getInputStream(); OutputStream out = s.getOutputStream()) {
                                int b = in.read();
                                if (b != -1) {
                                    out.write(b);
                                    out.flush();
                                }
                            } catch (Exception ignored) {
                            }
                        });
                    } catch (Exception e) {
                        break;
                    }
                }
            });
            acceptor.start();

            CountDownLatch gate = new CountDownLatch(1);
            Thread[] workers = new Thread[clients];
            for (int i = 0; i < clients; i++) {
                boolean isSlow = i < slowCount;
                workers[i] = new Thread(() -> {
                    try (Socket c = new Socket()) {
                        gate.await();
                        c.connect(new InetSocketAddress("127.0.0.1", port), 80);
                        c.setSoTimeout(80);
                        if (isSlow) Thread.sleep(120);
                        c.getOutputStream().write(1);
                        c.getOutputStream().flush();
                        if (!isSlow && c.getInputStream().read() == 1) {
                            fastOk.incrementAndGet();
                        }
                    } catch (Exception ignored) {
                    }
                });
                workers[i].start();
            }

            long start = System.nanoTime();
            gate.countDown();
            for (Thread w : workers) w.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            acceptor.join();
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
            System.out.println("fastOk=" + fastOk.get());
            System.out.println("expected=" + fastCount);
            System.out.println("ms=" + ms);
        }
    }
}
