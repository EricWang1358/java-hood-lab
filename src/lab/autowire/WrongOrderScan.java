package lab.autowire;

import java.util.LinkedHashMap;
import java.util.Map;

public class WrongOrderScan {
    interface Serializer { boolean isCustom(); }
    static class DefaultSerializer implements Serializer { @Override public boolean isCustom() { return false; } }
    static class CustomSerializer implements Serializer { @Override public boolean isCustom() { return true; } }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            Map<String, Serializer> ctx = new LinkedHashMap<>();
            // 错误顺序：自动装配早于用户配置执行，误判「容器里还没有」
            if (!ctx.containsKey("serializer")) {
                ctx.put("serializer", new DefaultSerializer());
            }
            // 用户自定义 Bean 后到，若不允许覆盖或只取首个 Bean，默认配置抢先占坑
            ctx.putIfAbsent("serializer", new CustomSerializer());
            if (ctx.get("serializer").isCustom()) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("custom_used=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
