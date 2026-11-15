package lab.authjwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CachedJwt {
    static final byte[] SECRET = "super-secret-jwt-key-256bits!".getBytes(StandardCharsets.UTF_8);
    static final ConcurrentHashMap<String, Boolean> VERIFIED_CACHE = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, Boolean> REVOKED_BLACKLIST = new ConcurrentHashMap<>();

    static String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
        String b64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String sig = Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(b64.getBytes(StandardCharsets.UTF_8)));
        return b64 + "." + sig;
    }

    static boolean verifyWithCache(String token, Mac mac) {
        if (REVOKED_BLACKLIST.containsKey(token)) return false;
        if (Boolean.TRUE.equals(VERIFIED_CACHE.get(token))) return true;
        int dot = token.indexOf('.');
        if (dot <= 0) return false;
        byte[] expectedSig = mac.doFinal(token.substring(0, dot).getBytes(StandardCharsets.UTF_8));
        byte[] actualSig = Base64.getUrlDecoder().decode(token.substring(dot + 1));
        boolean ok = MessageDigest.isEqual(expectedSig, actualSig);
        if (ok) VERIFIED_CACHE.putIfAbsent(token, Boolean.TRUE);
        return ok;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int perThread = 50_000;
        String token = sign("{\"sub\":\"user-1\",\"exp\":9999999999}");

        AtomicInteger ok = new AtomicInteger();
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                try {
                    Mac mac = Mac.getInstance("HmacSHA256");
                    mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
                    int localOk = 0;
                    for (int i = 0; i < perThread; i++) {
                        if (verifyWithCache(token, mac)) localOk++;
                    }
                    ok.addAndGet(localOk);
                } catch (Exception ignored) {
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok.get());
        System.out.println("expected=" + (threads * perThread));
        System.out.println("ms=" + ms);
    }
}
