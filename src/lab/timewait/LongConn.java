package lab.timewait;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class LongConn {
    public static void main(String[] args) throws Exception {
        int rounds = Integer.parseInt(args[0]);
        int ok = 0;
        long start = System.nanoTime();

        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress("127.0.0.1", 0));
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                try (Socket s = server.accept();
                     InputStream in = s.getInputStream();
                     OutputStream out = s.getOutputStream()) {
                    for (int i = 0; i < rounds; i++) {
                        int b = in.read();
                        if (b == -1) break;
                        out.write(b);
                        out.flush();
                    }
                } catch (Exception ignored) {
                }
            });
            srv.start();

            try (Socket client = new Socket("127.0.0.1", port);
                 OutputStream out = client.getOutputStream();
                 InputStream in = client.getInputStream()) {
                for (int i = 0; i < rounds; i++) {
                    out.write(1);
                    out.flush();
                    if (in.read() == 1) {
                        ok++;
                    }
                }
            }
            srv.join();
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + rounds);
        System.out.println("ms=" + ms);
    }
}
