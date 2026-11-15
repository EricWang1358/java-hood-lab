package lab.zerocopy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.StandardOpenOption;

public class MappedCopy {
    public static void main(String[] args) throws Exception {
        int kb = Integer.parseInt(args[0]);
        int totalBytes = kb * 1024;
        File tmp = File.createTempFile("zc-mmap-", ".bin");
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
            try (SocketChannel sc = SocketChannel.open(new InetSocketAddress("127.0.0.1", port));
                 FileChannel fc = FileChannel.open(tmp.toPath(), StandardOpenOption.READ)) {
                MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, totalBytes);
                while (mbb.hasRemaining()) {
                    sc.write(mbb);
                }
            }
            srv.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.println("bytes=" + recv[0]);
            System.out.println("expected=" + totalBytes);
            System.out.println("ms=" + ms);
        }
    }
}
