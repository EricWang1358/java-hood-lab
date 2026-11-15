package lab.nagle;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NoDelay {
    public static void main(String[] args) throws Exception {
        int rounds = Integer.parseInt(args[0]);
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                try (Socket s = server.accept();
                     InputStream in = s.getInputStream();
                     OutputStream out = s.getOutputStream()) {
                    s.setTcpNoDelay(true);
                    byte[] buf = new byte[8];
                    for (int i = 0; i < rounds; i++) {
                        int got = 0;
                        while (got < 8) {
                            int n = in.read(buf, got, 8 - got);
                            if (n == -1) return;
                            got += n;
                        }
                        out.write(1);
                        out.flush();
                    }
                } catch (Exception ignored) {
                }
            });
            srv.start();

            int acks = 0;
            long start = System.nanoTime();
            try (Socket c = new Socket("127.0.0.1", port);
                 OutputStream out = c.getOutputStream();
                 InputStream in = c.getInputStream()) {
                c.setTcpNoDelay(true);
                byte[] header = new byte[]{1, 2, 3, 4};
                byte[] body = new byte[]{5, 6, 7, 8};
                for (int i = 0; i < rounds; i++) {
                    out.write(header);
                    out.write(body);
                    out.flush();
                    if (in.read() == 1) {
                        acks++;
                    }
                }
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            srv.join();
            System.out.println("acks=" + acks);
            System.out.println("expected=" + rounds);
            System.out.println("ms=" + ms);
        }
    }
}
