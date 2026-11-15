package lab.selector;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleReactor {
    public static void main(String[] args) throws Exception {
        int conns = Integer.parseInt(args[0]);
        AtomicInteger ok = new AtomicInteger();

        try (Selector selector = Selector.open();
             ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress("127.0.0.1", 0), 512);
            int port = ((InetSocketAddress) ssc.getLocalAddress()).getPort();
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            Thread reactor = new Thread(() -> {
                int closed = 0;
                ByteBuffer buf = ByteBuffer.allocate(16);
                while (closed < conns) {
                    try {
                        selector.select(50);
                        Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                        while (it.hasNext()) {
                            SelectionKey key = it.next();
                            it.remove();
                            if (key.isAcceptable()) {
                                SocketChannel ch = ssc.accept();
                                if (ch != null) {
                                    ch.configureBlocking(false);
                                    ch.register(selector, SelectionKey.OP_READ);
                                }
                            } else if (key.isReadable()) {
                                SocketChannel ch = (SocketChannel) key.channel();
                                buf.clear();
                                int n = ch.read(buf);
                                if (n == -1) {
                                    key.cancel();
                                    ch.close();
                                    closed++;
                                } else if (n > 0) {
                                    buf.flip();
                                    while (buf.hasRemaining()) ch.write(buf);
                                }
                            }
                        }
                    } catch (Exception e) {
                        break;
                    }
                }
            });
            reactor.start();

            long start = System.nanoTime();
            Thread[] clients = new Thread[conns];
            for (int i = 0; i < conns; i++) {
                clients[i] = new Thread(() -> {
                    try (Socket c = new Socket("127.0.0.1", port)) {
                        c.getOutputStream().write(1);
                        c.getOutputStream().flush();
                        if (c.getInputStream().read() == 1) ok.incrementAndGet();
                    } catch (Exception ignored) {
                    }
                });
                clients[i].start();
            }
            for (Thread c : clients) c.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            reactor.join();
            System.out.println("ok=" + ok.get());
            System.out.println("expected=" + conns);
            System.out.println("ms=" + ms);
        }
    }
}
