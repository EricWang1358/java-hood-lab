package lab.niobuf;

import java.nio.ByteBuffer;

public class DirectByteBufferReuse {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        ByteBuffer buf = ByteBuffer.allocateDirect(16);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            buf.clear();
            buf.putInt(111);
            buf.putInt(222);
            buf.flip();
            if (buf.getInt() == 111 && buf.getInt() == 222) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
