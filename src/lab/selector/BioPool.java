package lab.selector;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BioPool {
    public static void main(String[] args) throws Exception {
        int conns = Integer.parseInt(args[0]);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        AtomicInteger ok = new AtomicInteger();

        try (ServerSocket server = new ServerSocket(0, 512)) {
            int port = server.getLocalPort();
            Thread acceptor = new Thread(() -> {
                for (int i = 0; i < conns; i++) {
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

            long start = System.nanoTime();
            Thread[] clients = new Thread[conns];
            for (int i = 0; i < conns; i++) {
                clients[i] = new Thread(() -> {
                    try (Socket c = new Socket("127.0.0.1", port)) {
                        c.getOutputStream().write(1);
                        c.getOutputStream().flush();
                        if (c.getInputStream().read() == 1) ok.incrementAndGet();
                    } catch (Exception ignored) {
                    }
                });
                clients[i].start();
            }
            for (Thread c : clients) c.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            acceptor.join();
            pool.shutdown();
            pool.awaitTermination(2, TimeUnit.SECONDS);
            System.out.println("ok=" + ok.get());
            System.out.println("expected=" + conns);
            System.out.println("ms=" + ms);
        }
    }
}
