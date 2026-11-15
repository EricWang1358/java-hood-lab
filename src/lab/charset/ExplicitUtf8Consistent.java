package lab.charset;

import java.nio.charset.StandardCharsets;

public class ExplicitUtf8Consistent {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        String src = "订单支付成功-Java实验";
        long start = System.nanoTime();
        int intact = 0;
        for (int i = 0; i < n; i++) {
            byte[] utf8Bytes = src.getBytes(StandardCharsets.UTF_8);
            String decoded = new String(utf8Bytes, StandardCharsets.UTF_8);
            if (src.equals(decoded)) intact++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("intact=" + intact);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
