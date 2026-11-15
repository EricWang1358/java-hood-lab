package lab.niobuf;

import java.nio.ByteBuffer;

public class FlipAndCompactSafe {
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
            buf.compact(); // 把未读的 222 挪到数组头部，position=4 继续追加写入
            buf.putInt(333);
            buf.flip();
            if (first == 111 && buf.getInt() == 222 && buf.getInt() == 333) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
