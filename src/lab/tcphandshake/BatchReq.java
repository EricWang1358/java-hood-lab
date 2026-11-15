package lab.tcphandshake;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BatchReq {
    public static void main(String[] args) throws Exception {
        int requests = Integer.parseInt(args[0]);
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                try (Socket s = server.accept();
                     InputStream in = s.getInputStream();
                     OutputStream out = s.getOutputStream()) {
                    byte[] buf = new byte[requests];
                    int read = 0;
                    while (read < requests) {
                        int n = in.read(buf, read, requests - read);
                        if (n == -1) break;
                        read += n;
                    }
                    out.write(buf, 0, read);
                    out.flush();
                } catch (Exception ignored) {
                }
            });
            srv.start();

            int ok = 0;
            long start = System.nanoTime();
            try (Socket client = new Socket("127.0.0.1", port);
                 OutputStream out = client.getOutputStream();
                 InputStream in = client.getInputStream()) {
                byte[] req = new byte[requests];
                for (int i = 0; i < requests; i++) req[i] = 1;
                out.write(req);
                out.flush();
                byte[] resp = new byte[requests];
                int got = 0;
                while (got < requests) {
                    int n = in.read(resp, got, requests - got);
                    if (n == -1) break;
                    got += n;
                }
                ok = got;
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            srv.join();
            System.out.println("ok=" + ok);
            System.out.println("expected=" + requests);
            System.out.println("ms=" + ms);
        }
    }
}
