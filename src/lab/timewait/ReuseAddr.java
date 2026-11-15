package lab.timewait;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ReuseAddr {
    public static void main(String[] args) throws Exception {
        int rounds = Integer.parseInt(args[0]);
        int ok = 0;
        long start = System.nanoTime();

        int port;
        try (ServerSocket probe = new ServerSocket(0)) {
            port = probe.getLocalPort();
        }

        for (int i = 0; i < rounds; i++) {
            try (ServerSocket server = new ServerSocket()) {
                server.setReuseAddress(true);
                server.bind(new InetSocketAddress("127.0.0.1", port));
                Thread cli = new Thread(() -> {
                    try (Socket c = new Socket("127.0.0.1", port)) {
                        c.getInputStream().read();
                    } catch (Exception ignored) {
                    }
                });
                cli.start();
                try (Socket accepted = server.accept()) {
                    // Server actively closes, but SO_REUSEADDR lets listener rebind
                }
                cli.join();
                ok++;
            } catch (Exception ignored) {
            }
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + rounds);
        System.out.println("ms=" + ms);
    }
}
