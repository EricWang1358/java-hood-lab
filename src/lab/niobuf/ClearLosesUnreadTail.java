package lab.niobuf;

import java.nio.ByteBuffer;

public class ClearLosesUnreadTail {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        ByteBuffer buf = ByteBuffer.allocate(16);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            buf.clear();
            buf.putInt(111);
            buf.putInt(222);
            buf.flip();
            int first = buf.getInt();
            // 还有 222 没读完却误调 clear() 而不是 compact()，导致未读半包丢失
            buf.clear();
            buf.putInt(333);
            buf.flip();
            if (first == 111 && buf.getInt() == 222) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
