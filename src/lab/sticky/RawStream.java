package lab.sticky;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class RawStream {
    public static void main(String[] args) throws Exception {
        int msgs = Integer.parseInt(args[0]);
        AtomicInteger exact = new AtomicInteger();
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                try (Socket s = server.accept();
                     InputStream in = s.getInputStream()) {
                    byte[] buf = new byte[256];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        String chunk = new String(buf, 0, n, StandardCharsets.UTF_8);
                        if (chunk.matches("MSG-\\d{4}")) {
                            exact.incrementAndGet();
                        }
                    }
                } catch (Exception ignored) {
                }
            });
            srv.start();

            long start = System.nanoTime();
            try (Socket c = new Socket("127.0.0.1", port);
                 OutputStream out = c.getOutputStream()) {
                for (int i = 0; i < msgs; i++) {
                    String payload = String.format("MSG-%04d", i);
                    out.write(payload.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
            }
            srv.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.println("exact=" + exact.get());
            System.out.println("expected=" + msgs);
            System.out.println("ms=" + ms);
        }
    }
}
