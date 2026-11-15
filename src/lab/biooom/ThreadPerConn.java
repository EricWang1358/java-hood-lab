package lab.biooom;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPerConn {
    public static void main(String[] args) throws Exception {
        int clients = Integer.parseInt(args[0]);
        int slowCount = Math.min(4, clients / 2);
        int fastCount = clients - slowCount;
        AtomicInteger fastOk = new AtomicInteger();

        try (ServerSocket server = new ServerSocket(0, 256)) {
            int port = server.getLocalPort();
            List<Thread> srvHandlers = new ArrayList<>();
            Thread acceptor = new Thread(() -> {
                for (int i = 0; i < clients; i++) {
                    try {
                        Socket s = server.accept();
                        Thread h = new Thread(() -> {
                            try (s; InputStream in = s.getInputStream(); OutputStream out = s.getOutputStream()) {
                                int b = in.read();
                                if (b != -1) {
                                    out.write(b);
                                    out.flush();
                                }
                            } catch (Exception ignored) {
                            }
                        });
                        h.start();
                        srvHandlers.add(h);
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
                        c.connect(new InetSocketAddress("127.0.0.1", port), 150);
                        c.setSoTimeout(150);
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
            for (Thread h : srvHandlers) h.join();
            System.out.println("fastOk=" + fastOk.get());
            System.out.println("expected=" + fastCount);
            System.out.println("ms=" + ms);
        }
    }
}
