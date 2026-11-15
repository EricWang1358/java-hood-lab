package lab.tcphandshake;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ShortConn {
    public static void main(String[] args) throws Exception {
        int requests = Integer.parseInt(args[0]);
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                for (int i = 0; i < requests; i++) {
                    try (Socket s = server.accept();
                         InputStream in = s.getInputStream();
                         OutputStream out = s.getOutputStream()) {
                        int b = in.read();
                        if (b != -1) {
                            out.write(b);
                            out.flush();
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
            srv.start();

            int ok = 0;
            long start = System.nanoTime();
            for (int i = 0; i < requests; i++) {
                try (Socket client = new Socket("127.0.0.1", port);
                     OutputStream out = client.getOutputStream();
                     InputStream in = client.getInputStream()) {
                    out.write(1);
                    out.flush();
                    if (in.read() == 1) {
                        ok++;
                    }
                }
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            srv.join();
            System.out.println("ok=" + ok);
            System.out.println("expected=" + requests);
            System.out.println("ms=" + ms);
        }
    }
}
