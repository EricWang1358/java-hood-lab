package lab.zerocopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BufferedCopy {
    public static void main(String[] args) throws Exception {
        int kb = Integer.parseInt(args[0]);
        int totalBytes = kb * 1024;
        File tmp = File.createTempFile("zc-buf-", ".bin");
        tmp.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write(new byte[totalBytes]);
        }

        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            final long[] recv = {0};
            Thread srv = new Thread(() -> {
                try (Socket s = server.accept();
                     InputStream in = s.getInputStream()) {
                    byte[] buf = new byte[65536];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        recv[0] += n;
                    }
                } catch (Exception ignored) {
                }
            });
            srv.start();

            long start = System.nanoTime();
            try (Socket c = new Socket("127.0.0.1", port);
                 OutputStream out = c.getOutputStream();
                 FileInputStream fis = new FileInputStream(tmp)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = fis.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
                out.flush();
            }
            srv.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.println("bytes=" + recv[0]);
            System.out.println("expected=" + totalBytes);
            System.out.println("ms=" + ms);
        }
    }
}
