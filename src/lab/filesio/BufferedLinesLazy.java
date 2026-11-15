package lab.filesio;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class BufferedLinesLazy {
    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        byte[] data = new byte[n];
        for (int i = 0; i < n; i++) data[i] = (byte) ((i & 63) == 63 ? '\n' : 'a');
        long start = System.nanoTime();
        long chars = 0;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8), 8192)) {
            String line;
            while ((line = br.readLine()) != null) {
                chars += line.length() + 1;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("bytes=" + Math.min(chars, n));
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
