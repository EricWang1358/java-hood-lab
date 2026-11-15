package lab.charset;

import java.nio.charset.StandardCharsets;

public class ReplacementCharIrreversible {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        String src = "订单支付成功-Java实验";
        long start = System.nanoTime();
        int intact = 0;
        for (int i = 0; i < n; i++) {
            // 用 ISO-8859-1 解码再重新编码为 UTF-8，非 ASCII 字节已被永久破坏
            byte[] utf8 = src.getBytes(StandardCharsets.UTF_8);
            String iso = new String(utf8, StandardCharsets.ISO_8859_1);
            byte[] reEncoded = iso.getBytes(StandardCharsets.UTF_8);
            String recovered = new String(reEncoded, StandardCharsets.UTF_8);
            if (src.equals(recovered)) intact++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("intact=" + intact);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
