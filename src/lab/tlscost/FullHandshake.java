package lab.tlscost;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class FullHandshake {
    public static void main(String[] args) throws Exception {
        int requests = Integer.parseInt(args[0]);
        KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
        rsaGen.initialize(2048);
        KeyPair certKey = rsaGen.generateKeyPair();

        byte[] payload = "GET /api/order".getBytes(StandardCharsets.UTF_8);
        byte[] iv = new byte[12];

        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            Thread srv = new Thread(() -> {
                try {
                    KeyPairGenerator ecGen = KeyPairGenerator.getInstance("EC");
                    ecGen.initialize(256);
                    for (int i = 0; i < requests; i++) {
                        try (Socket s = server.accept();
                             DataInputStream in = new DataInputStream(s.getInputStream());
                             DataOutputStream out = new DataOutputStream(s.getOutputStream())) {
                            // 1. Server sends ephemeral EC public key + RSA signature (CertificateVerify)
                            KeyPair srvEc = ecGen.generateKeyPair();
                            byte[] srvPub = srvEc.getPublic().getEncoded();
                            Signature sig = Signature.getInstance("SHA256withRSA");
                            sig.initSign(certKey.getPrivate());
                            sig.update(srvPub);
                            byte[] sigBytes = sig.sign();
                            out.writeInt(srvPub.length);
                            out.write(srvPub);
                            out.writeInt(sigBytes.length);
                            out.write(sigBytes);
                            out.flush();

                            // 2. Receive client ephemeral EC public key & derive shared AES key
                            int cLen = in.readInt();
                            byte[] cliPub = new byte[cLen];
                            in.readFully(cliPub);
                            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
                            ka.init(srvEc.getPrivate());
                            ka.doPhase(KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(cliPub)), true);
                            byte[] secret = ka.generateSecret();
                            SecretKeySpec aes = new SecretKeySpec(secret, 0, 16, "AES");

                            // 3. Decrypt request & encrypt response with AES-GCM
                            int ctLen = in.readInt();
                            byte[] ct = new byte[ctLen];
                            in.readFully(ct);
                            Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
                            dec.init(Cipher.DECRYPT_MODE, aes, new GCMParameterSpec(128, iv));
                            dec.doFinal(ct);

                            Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
                            enc.init(Cipher.ENCRYPT_MODE, aes, new GCMParameterSpec(128, iv));
                            byte[] respCt = enc.doFinal("OK".getBytes(StandardCharsets.UTF_8));
                            out.writeInt(respCt.length);
                            out.write(respCt);
                            out.flush();
                        }
                    }
                } catch (Exception ignored) {
                }
            });
            srv.start();

            KeyPairGenerator ecGen = KeyPairGenerator.getInstance("EC");
            ecGen.initialize(256);
            int ok = 0;
            long start = System.nanoTime();
            for (int i = 0; i < requests; i++) {
                try (Socket c = new Socket("127.0.0.1", port);
                     DataOutputStream out = new DataOutputStream(c.getOutputStream());
                     DataInputStream in = new DataInputStream(c.getInputStream())) {
                    int srvLen = in.readInt();
                    byte[] srvPub = new byte[srvLen];
                    in.readFully(srvPub);
                    int sigLen = in.readInt();
                    byte[] sigBytes = new byte[sigLen];
                    in.readFully(sigBytes);

                    Signature ver = Signature.getInstance("SHA256withRSA");
                    ver.initVerify(certKey.getPublic());
                    ver.update(srvPub);
                    if (!ver.verify(sigBytes)) continue;

                    KeyPair cliEc = ecGen.generateKeyPair();
                    byte[] cliPub = cliEc.getPublic().getEncoded();
                    out.writeInt(cliPub.length);
                    out.write(cliPub);

                    KeyAgreement ka = KeyAgreement.getInstance("ECDH");
                    ka.init(cliEc.getPrivate());
                    ka.doPhase(KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(srvPub)), true);
                    SecretKeySpec aes = new SecretKeySpec(ka.generateSecret(), 0, 16, "AES");

                    Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
                    enc.init(Cipher.ENCRYPT_MODE, aes, new GCMParameterSpec(128, iv));
                    byte[] ct = enc.doFinal(payload);
                    out.writeInt(ct.length);
                    out.write(ct);
                    out.flush();

                    int rLen = in.readInt();
                    byte[] rCt = new byte[rLen];
                    in.readFully(rCt);
                    Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
                    dec.init(Cipher.DECRYPT_MODE, aes, new GCMParameterSpec(128, iv));
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
