package lab.autowire;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConditionalOnMissing {
    interface Serializer { boolean isCustom(); }
    static class DefaultSerializer implements Serializer { @Override public boolean isCustom() { return false; } }
    static class CustomSerializer implements Serializer { @Override public boolean isCustom() { return true; } }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            Map<String, Serializer> ctx = new LinkedHashMap<>();
            // 1. 先解析用户自己的 @Configuration
            ctx.put("serializer", new CustomSerializer());
            // 2. 最后加载 AutoConfiguration，@ConditionalOnMissingBean 检测到已有则退让
            if (!ctx.containsKey("serializer")) {
                ctx.put("serializer", new DefaultSerializer());
            }
            if (ctx.get("serializer").isCustom()) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("custom_used=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
