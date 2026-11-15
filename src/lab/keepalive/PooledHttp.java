package lab.keepalive;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PooledHttp {
    static final byte[] RESP = ("HTTP/1.1 200 OK\r\nContent-Length: 2\r\nConnection: keep-alive\r\n\r\nOK")
        .getBytes(StandardCharsets.UTF_8);

    static void readHeaders(InputStream in) throws Exception {
        int matched = 0;
        while (matched < 4) {
            int b = in.read();
            if (b == -1) return;
            if ((matched == 0 || matched == 2) && b == '\r') matched++;
            else if ((matched == 1 || matched == 3) && b == '\n') matched++;
            else matched = (b == '\r') ? 1 : 0;
        }
    }

    public static void main(String[] args) throws Exception {
        int requests = Integer.parseInt(args[0]);
        int poolSize = 4;
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread[] srvWorkers = new Thread[poolSize];
            for (int w = 0; w < poolSize; w++) {
                srvWorkers[w] = new Thread(() -> {
                    try (Socket s = server.accept();
                         InputStream in = new BufferedInputStream(s.getInputStream());
                         OutputStream out = s.getOutputStream()) {
                        while (true) {
                            in.mark(1);
                            if (in.read() == -1) break;
                            in.reset();
                            readHeaders(in);
                            out.write(RESP);
                            out.flush();
                        }
                    } catch (Exception ignored) {
                    }
                });
                srvWorkers[w].start();
            }

            BlockingQueue<Socket> pool = new ArrayBlockingQueue<>(poolSize);
            for (int w = 0; w < poolSize; w++) {
                pool.add(new Socket("127.0.0.1", port));
            }
            byte[] req = ("GET /ping HTTP/1.1\r\nHost: localhost\r\nConnection: keep-alive\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8);

            int ok = 0;
            long start = System.nanoTime();
            for (int i = 0; i < requests; i++) {
                Socket c = pool.take();
                c.getOutputStream().write(req);
                c.getOutputStream().flush();
                InputStream in = c.getInputStream();
                readHeaders(in);
                if (in.read() == 'O' && in.read() == 'K') ok++;
                pool.put(c);
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            for (Socket c : pool) c.close();
            for (Thread t : srvWorkers) t.join();
            System.out.println("ok=" + ok);
            System.out.println("expected=" + requests);
            System.out.println("ms=" + ms);
        }
    }
}
