package lab.directbuf;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class HeapBuffer {
    public static void main(String[] args) throws Exception {
        int chunks = Integer.parseInt(args[0]);
        int chunkSize = 16384;
        long expected = (long) chunks * chunkSize;

        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress("127.0.0.1", 0));
            int port = ((InetSocketAddress) server.getLocalAddress()).getPort();
            final long[] recv = {0};
            Thread srv = new Thread(() -> {
                try (SocketChannel sc = server.accept()) {
                    ByteBuffer buf = ByteBuffer.allocate(65536);
                    while (true) {
                        buf.clear();
                        int n = sc.read(buf);
                        if (n == -1) break;
                        recv[0] += n;
                    }
                } catch (Exception ignored) {
                }
            });
            srv.start();

            long start = System.nanoTime();
            try (SocketChannel client = SocketChannel.open(new InetSocketAddress("127.0.0.1", port))) {
                ByteBuffer buf = ByteBuffer.allocate(chunkSize);
                for (int i = 0; i < chunks; i++) {
                    buf.clear();
                    while (buf.hasRemaining()) client.write(buf);
                }
            }
            srv.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.println("bytes=" + recv[0]);
            System.out.println("expected=" + expected);
            System.out.println("ms=" + ms);
        }
    }
}
