package lab.autowire;

import java.util.ArrayList;
import java.util.List;

public class DuplicateBeanConflict {
    interface Serializer { boolean isCustom(); }
    static class DefaultSerializer implements Serializer { @Override public boolean isCustom() { return false; } }
    static class CustomSerializer implements Serializer { @Override public boolean isCustom() { return true; } }

    static Serializer getByType(List<Serializer> beans) {
        if (beans.size() > 1) throw new IllegalStateException("NoUniqueBeanDefinitionException");
        return beans.get(0);
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            List<Serializer> ctx = new ArrayList<>();
            ctx.add(new CustomSerializer());
            ctx.add(new DefaultSerializer()); // 没加 @ConditionalOnMissingBean，两个全注册进去了
            try {
                if (getByType(ctx).isCustom()) ok++;
            } catch (IllegalStateException ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("custom_used=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
