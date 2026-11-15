package lab.corspreflight;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class SameOriginProxy {
    static final byte[] POST_RESP = ("HTTP/1.1 200 OK\r\nContent-Length: 2\r\nConnection: keep-alive\r\n\r\nOK").getBytes(StandardCharsets.UTF_8);

    static String readHeaders(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        int matched = 0;
        while (matched < 4) {
            int b = in.read();
            if (b == -1) return null;
            sb.append((char) b);
            if ((matched == 0 || matched == 2) && b == '\r') matched++;
            else if ((matched == 1 || matched == 3) && b == '\n') matched++;
            else matched = (b == '\r') ? 1 : 0;
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        int requests = Integer.parseInt(args[0]);
        AtomicInteger httpCalls = new AtomicInteger();
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                try (Socket s = server.accept();
                     InputStream in = new BufferedInputStream(s.getInputStream());
                     OutputStream out = s.getOutputStream()) {
                    while (readHeaders(in) != null) {
                        httpCalls.incrementAndGet();
                        out.write(POST_RESP);
                        out.flush();
                    }
                } catch (Exception ignored) {
                }
            });
            srv.start();

            byte[] postReq = ("POST /api/pay HTTP/1.1\r\nHost: app.example.com\r\nX-Token: abc\r\n\r\n").getBytes(StandardCharsets.UTF_8);

            long start = System.nanoTime();
            try (Socket c = new Socket("127.0.0.1", port);
                 OutputStream out = c.getOutputStream();
                 InputStream in = new BufferedInputStream(c.getInputStream())) {
                for (int i = 0; i < requests; i++) {
                    out.write(postReq);
                    out.flush();
                    readHeaders(in);
                    in.read();
                    in.read();
                }
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            srv.join();
            System.out.println("httpCalls=" + httpCalls.get());
            System.out.println("expected=" + requests);
            System.out.println("ms=" + ms);
        }
    }
}
