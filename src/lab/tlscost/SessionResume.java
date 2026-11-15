package lab.tlscost;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SessionResume {
    public static void main(String[] args) throws Exception {
        int requests = Integer.parseInt(args[0]);
        SecretKeySpec resumedKey = new SecretKeySpec(new byte[16], "AES");
        byte[] iv = new byte[12];
        byte[] payload = "GET /api/order".getBytes(StandardCharsets.UTF_8);

        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                for (int i = 0; i < requests; i++) {
                    try (Socket s = server.accept();
                         DataInputStream in = new DataInputStream(s.getInputStream());
                         DataOutputStream out = new DataOutputStream(s.getOutputStream())) {
                        int ctLen = in.readInt();
                        byte[] ct = new byte[ctLen];
                        in.readFully(ct);
                        Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
                        dec.init(Cipher.DECRYPT_MODE, resumedKey, new GCMParameterSpec(128, iv));
                        dec.doFinal(ct);

                        Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
                        enc.init(Cipher.ENCRYPT_MODE, resumedKey, new GCMParameterSpec(128, iv));
                        byte[] respCt = enc.doFinal("OK".getBytes(StandardCharsets.UTF_8));
                        out.writeInt(respCt.length);
                        out.write(respCt);
                        out.flush();
                    } catch (Exception ignored) {
                    }
                }
            });
            srv.start();

            int ok = 0;
            long start = System.nanoTime();
            for (int i = 0; i < requests; i++) {
                try (Socket c = new Socket("127.0.0.1", port);
                     DataOutputStream out = new DataOutputStream(c.getOutputStream());
                     DataInputStream in = new DataInputStream(c.getInputStream())) {
                    Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
                    enc.init(Cipher.ENCRYPT_MODE, resumedKey, new GCMParameterSpec(128, iv));
                    byte[] ct = enc.doFinal(payload);
                    out.writeInt(ct.length);
                    out.write(ct);
                    out.flush();

                    int rLen = in.readInt();
                    byte[] rCt = new byte[rLen];
                    in.readFully(rCt);
                    Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
                    dec.init(Cipher.DECRYPT_MODE, resumedKey, new GCMParameterSpec(128, iv));
                    if (dec.doFinal(rCt).length == 2) ok++;
                }
            }
            long ms = (System.nanoTime() - start) / 1_000_000;
            srv.join();
            System.out.println("ok=" + ok);
            System.out.println("expected=" + requests);
            System.out.println("ms=" + ms);
        }
    }
}
