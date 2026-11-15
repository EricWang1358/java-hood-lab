package lab.sticky;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class LengthPrefix {
    public static void main(String[] args) throws Exception {
        int msgs = Integer.parseInt(args[0]);
        AtomicInteger exact = new AtomicInteger();
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                try (Socket s = server.accept();
                     DataInputStream in = new DataInputStream(s.getInputStream())) {
                    while (true) {
                        int len;
                        try {
                            len = in.readInt();
                        } catch (EOFException eof) {
                            break;
                        }
                        byte[] body = new byte[len];
                        in.readFully(body);
                        String msg = new String(body, StandardCharsets.UTF_8);
                        if (msg.matches("MSG-\\d{4}")) {
                            exact.incrementAndGet();
                        }
                    }
                } catch (Exception ignored) {
                }
            });
            srv.start();

            long start = System.nanoTime();
            try (Socket c = new Socket("127.0.0.1", port);
                 DataOutputStream out = new DataOutputStream(c.getOutputStream())) {
                for (int i = 0; i < msgs; i++) {
                    byte[] body = String.format("MSG-%04d", i).getBytes(StandardCharsets.UTF_8);
                    out.writeInt(body.length);
                    out.write(body);
                }
                out.flush();
            }
            srv.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.println("exact=" + exact.get());
            System.out.println("expected=" + msgs);
            System.out.println("ms=" + ms);
        }
    }
}
