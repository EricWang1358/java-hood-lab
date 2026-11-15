package lab.niobuf;

import java.nio.ByteBuffer;

public class ForgetFlipReadsGarbage {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        ByteBuffer buf = ByteBuffer.allocate(16);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            buf.clear();
            buf.putInt(12345);
            // 忘记 flip()，position 停在 4，读取到的是未写入的 0！
            if (buf.hasRemaining() && buf.getInt() == 12345) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
