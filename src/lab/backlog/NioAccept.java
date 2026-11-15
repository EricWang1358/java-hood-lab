package lab.backlog;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class NioAccept {
    public static void main(String[] args) throws Exception {
        int clients = Integer.parseInt(args[0]);
        AtomicInteger connected = new AtomicInteger();
        try (Selector selector = Selector.open();
             ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress("127.0.0.1", 0), 128);
            int port = ((InetSocketAddress) ssc.getLocalAddress()).getPort();
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            Thread srv = new Thread(() -> {
                int closed = 0;
                ByteBuffer buf = ByteBuffer.allocate(16);
                while (closed < clients) {
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
                                }
                            }
                        }
                    } catch (Exception e) {
                        break;
                    }
                }
            });
            srv.start();

            CountDownLatch startGate = new CountDownLatch(1);
            Thread[] workers = new Thread[clients];
            for (int i = 0; i < clients; i++) {
                workers[i] = new Thread(() -> {
                    try {
                        startGate.await();
                        try (Socket c = new Socket()) {
                            c.connect(new InetSocketAddress("127.0.0.1", port), 40);
                            c.getOutputStream().write(1);
                            connected.incrementAndGet();
                        }
                    } catch (Exception ignored) {
                    }
                });
                workers[i].start();
            }
            long start = System.nanoTime();
            startGate.countDown();
            for (Thread w : workers) w.join();
            long ms = (System.nanoTime() - start) / 1_000_000;
            srv.join();
            System.out.println("connected=" + connected.get());
            System.out.println("expected=" + clients);
            System.out.println("ms=" + ms);
        }
    }
}
