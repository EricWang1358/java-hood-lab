package lab.serial;

import java.nio.charset.StandardCharsets;

public class LightweightJsonText {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            String json = "{\"id\":" + i + ",\"code\":\"ORD-2025\"}";
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            String back = new String(bytes, StandardCharsets.UTF_8);
            if (back.length() > 15) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
