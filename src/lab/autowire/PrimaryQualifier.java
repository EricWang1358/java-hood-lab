package lab.autowire;

import java.util.HashMap;
import java.util.Map;

public class PrimaryQualifier {
    interface Serializer { boolean isCustom(); }
    static class DefaultSerializer implements Serializer { @Override public boolean isCustom() { return false; } }
    static class CustomSerializer implements Serializer { @Override public boolean isCustom() { return true; } }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            Map<String, Serializer> ctx = new HashMap<>();
            ctx.put("defaultSerializer", new DefaultSerializer());
            ctx.put("customSerializer", new CustomSerializer());
            // 多实现共存时，通过 @Qualifier("customSerializer") 或 @Primary 精确路由
            Serializer chosen = ctx.get("customSerializer");
            if (chosen.isCustom()) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("custom_used=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
