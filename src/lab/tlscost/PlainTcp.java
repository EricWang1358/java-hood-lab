package lab.tlscost;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PlainTcp {
    public static void main(String[] args) throws Exception {
        int requests = Integer.parseInt(args[0]);
        byte[] payload = "GET /api/order".getBytes(StandardCharsets.UTF_8);
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                for (int i = 0; i < requests; i++) {
                    try (Socket s = server.accept();
                         DataInputStream in = new DataInputStream(s.getInputStream());
                         DataOutputStream out = new DataOutputStream(s.getOutputStream())) {
                        int len = in.readInt();
                        byte[] buf = new byte[len];
                        in.readFully(buf);
                        out.writeInt(2);
                        out.writeBytes("OK");
                        out.flush();
                    } catch (Exception ignored) {
                    }
                }
            });
            srv.start();

            int ok = 0;
            long start = System.nanoTime();
            for (int i = 0; i < requests; i++) {
                try (Socket c = new Socket("127.0.0.1", port);
                     DataOutputStream out = new DataOutputStream(c.getOutputStream());
                     DataInputStream in = new DataInputStream(c.getInputStream())) {
                    out.writeInt(payload.length);
                    out.write(payload);
                    out.flush();
                    int rlen = in.readInt();
                    byte[] resp = new byte[rlen];
                    in.readFully(resp);
                    if (rlen == 2) ok++;
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
