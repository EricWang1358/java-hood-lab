package lab.filesio;

import java.io.ByteArrayInputStream;

public class UnbufferedByteByByte {
    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        byte[] data = new byte[n];
        for (int i = 0; i < n; i++) data[i] = (byte) ('a' + (i % 26));
        long start = System.nanoTime();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        int count = 0;
        while (in.read() != -1) count++;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("bytes=" + count);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
