package lab.keepalive;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CloseConn {
    static final byte[] RESP = ("HTTP/1.1 200 OK\r\nContent-Length: 2\r\nConnection: close\r\n\r\nOK")
        .getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) throws Exception {
        int requests = Integer.parseInt(args[0]);
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                for (int i = 0; i < requests; i++) {
                    try (Socket s = server.accept();
                         BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                         OutputStream out = s.getOutputStream()) {
                        String line;
                        while ((line = br.readLine()) != null && !line.isEmpty()) {}
                        out.write(RESP);
                        out.flush();
                    } catch (Exception ignored) {
                    }
                }
            });
            srv.start();

            byte[] req = ("GET /ping HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8);
            int ok = 0;
            long start = System.nanoTime();
            for (int i = 0; i < requests; i++) {
                try (Socket c = new Socket("127.0.0.1", port);
                     OutputStream out = c.getOutputStream();
                     BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
                    out.write(req);
                    out.flush();
                    String status = br.readLine();
                    if (status != null && status.contains("200")) ok++;
                    while (br.readLine() != null) {}
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
