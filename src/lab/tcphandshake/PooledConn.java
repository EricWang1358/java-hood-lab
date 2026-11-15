package lab.tcphandshake;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PooledConn {
    public static void main(String[] args) throws Exception {
        int requests = Integer.parseInt(args[0]);
        int poolSize = 4;
        int perConn = requests / poolSize;
        int rem = requests % poolSize;

        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread[] srvWorkers = new Thread[poolSize];
            for (int w = 0; w < poolSize; w++) {
                int count = perConn + (w < rem ? 1 : 0);
                srvWorkers[w] = new Thread(() -> {
                    try (Socket s = server.accept();
                         InputStream in = s.getInputStream();
                         OutputStream out = s.getOutputStream()) {
                        while (true) {
                            int b = in.read();
                            if (b == -1) break;
                            out.write(b);
                            out.flush();
                        }
                    } catch (Exception ignored) {
                    }
                });
                srvWorkers[w].start();
            }

            BlockingQueue<Socket> pool = new ArrayBlockingQueue<>(poolSize);
            for (int w = 0; w < poolSize; w++) {
                pool.add(new Socket("127.0.0.1", port));
            }

            int ok = 0;
            long start = System.nanoTime();
            for (int i = 0; i < requests; i++) {
                Socket client = pool.take();
                client.getOutputStream().write(1);
                client.getOutputStream().flush();
                if (client.getInputStream().read() == 1) {
                    ok++;
                }
                pool.put(client);
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            for (Socket s : pool) {
                s.close();
            }
            for (Thread t : srvWorkers) {
                t.join();
            }
            System.out.println("ok=" + ok);
            System.out.println("expected=" + requests);
            System.out.println("ms=" + ms);
        }
    }
}
