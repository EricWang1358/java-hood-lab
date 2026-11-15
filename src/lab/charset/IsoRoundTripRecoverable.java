package lab.charset;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class IsoRoundTripRecoverable {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Charset iso = StandardCharsets.ISO_8859_1;
        String src = "订单支付成功-Java实验";
        long start = System.nanoTime();
        int intact = 0;
        for (int i = 0; i < n; i++) {
            byte[] rawUtf8 = src.getBytes(StandardCharsets.UTF_8);
            // 仅当中间环节按 ISO-8859-1 单字节无损透传时，按原编码还原字节才可救回
            String wrongIso = new String(rawUtf8, iso);
            String fixed = new String(wrongIso.getBytes(iso), StandardCharsets.UTF_8);
            if (src.equals(fixed)) intact++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("intact=" + intact);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
