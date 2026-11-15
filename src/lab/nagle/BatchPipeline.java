package lab.nagle;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BatchPipeline {
    public static void main(String[] args) throws Exception {
        int rounds = Integer.parseInt(args[0]);
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                try (Socket s = server.accept();
                     InputStream in = s.getInputStream();
                     OutputStream out = s.getOutputStream()) {
                    byte[] buf = new byte[8 * rounds];
                    int got = 0;
                    while (got < buf.length) {
                        int n = in.read(buf, got, buf.length - got);
                        if (n == -1) return;
                        got += n;
                    }
                    byte[] ack = new byte[rounds];
                    for (int i = 0; i < rounds; i++) ack[i] = 1;
                    out.write(ack);
                    out.flush();
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
                byte[] batch = new byte[8 * rounds];
                out.write(batch);
                out.flush();
                byte[] resp = new byte[rounds];
                int got = 0;
                while (got < rounds) {
                    int n = in.read(resp, got, rounds - got);
                    if (n == -1) break;
                    got += n;
                }
                acks = got;
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            srv.join();
            System.out.println("acks=" + acks);
            System.out.println("expected=" + rounds);
            System.out.println("ms=" + ms);
        }
    }
}
