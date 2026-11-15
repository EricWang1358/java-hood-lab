package lab.selector;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiReactor {
    public static void main(String[] args) throws Exception {
        int conns = Integer.parseInt(args[0]);
        int workerCount = 4;
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger closed = new AtomicInteger();

        Selector[] workers = new Selector[workerCount];
        @SuppressWarnings("unchecked")
        ConcurrentLinkedQueue<SocketChannel>[] queues = new ConcurrentLinkedQueue[workerCount];
        for (int i = 0; i < workerCount; i++) {
            workers[i] = Selector.open();
            queues[i] = new ConcurrentLinkedQueue<>();
        }

        Thread[] workerThreads = new Thread[workerCount];
        for (int w = 0; w < workerCount; w++) {
            final int idx = w;
            workerThreads[w] = new Thread(() -> {
                ByteBuffer buf = ByteBuffer.allocate(16);
                Selector sel = workers[idx];
                while (closed.get() < conns) {
                    try {
                        sel.select(20);
                        SocketChannel pending;
                        while ((pending = queues[idx].poll()) != null) {
                            pending.configureBlocking(false);
                            pending.register(sel, SelectionKey.OP_READ);
                        }
                        Iterator<SelectionKey> it = sel.selectedKeys().iterator();
                        while (it.hasNext()) {
                            SelectionKey key = it.next();
                            it.remove();
                            if (key.isReadable()) {
                                SocketChannel ch = (SocketChannel) key.channel();
                                buf.clear();
                                int n = ch.read(buf);
                                if (n == -1) {
                                    key.cancel();
                                    ch.close();
                                    closed.incrementAndGet();
                                } else if (n > 0) {
                                    buf.flip();
                                    while (buf.hasRemaining()) ch.write(buf);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
            workerThreads[w].start();
        }

        try (Selector boss = Selector.open();
             ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress("127.0.0.1", 0), 512);
            int port = ((InetSocketAddress) ssc.getLocalAddress()).getPort();
            ssc.register(boss, SelectionKey.OP_ACCEPT);

            Thread bossThread = new Thread(() -> {
                int accepted = 0;
                while (accepted < conns) {
                    try {
                        boss.select(50);
                        Iterator<SelectionKey> it = boss.selectedKeys().iterator();
                        while (it.hasNext()) {
                            SelectionKey key = it.next();
                            it.remove();
                            if (key.isAcceptable()) {
                                SocketChannel ch = ssc.accept();
                                if (ch != null) {
                                    int idx = accepted % workerCount;
                                    queues[idx].add(ch);
                                    workers[idx].wakeup();
                                    accepted++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        break;
                    }
                }
            });
            bossThread.start();

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
            bossThread.join();
            for (int w = 0; w < workerCount; w++) workers[w].wakeup();
            for (Thread wt : workerThreads) wt.join();
            for (Selector sel : workers) sel.close();
            System.out.println("ok=" + ok.get());
            System.out.println("expected=" + conns);
            System.out.println("ms=" + ms);
        }
    }
}
