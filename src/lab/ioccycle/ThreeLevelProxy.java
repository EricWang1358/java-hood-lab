package lab.ioccycle;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ThreeLevelProxy {
    interface ServiceA { boolean isProxy(); }
    static class ServiceAImpl implements ServiceA {
        ServiceB b;
        @Override public boolean isProxy() { return false; }
    }
    static class ServiceB { ServiceA a; }

    static ServiceA wrapProxy(ServiceA target) {
        return (ServiceA) Proxy.newProxyInstance(
            ServiceA.class.getClassLoader(),
            new Class<?>[]{ServiceA.class},
            (p, m, a) -> m.getName().equals("isProxy") ? true : m.invoke(target, a)
        );
    }

    static Object getBean(String name, Map<String, Object> l1, Map<String, Object> l2, Map<String, Supplier<Object>> l3) {
        Object bean = l1.get(name);
        if (bean != null) return bean;
        bean = l2.get(name);
        if (bean != null) return bean;
        Supplier<Object> factory = l3.remove(name);
        if (factory != null) {
            bean = factory.get(); // 发生循环依赖时，触发三级缓存工厂提前生成代理
            l2.put(name, bean);
        }
        return bean;
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            Map<String, Object> l1 = new HashMap<>();
            Map<String, Object> l2 = new HashMap<>();
            Map<String, Supplier<Object>> l3 = new HashMap<>();

            ServiceAImpl rawA = new ServiceAImpl();
            l3.put("a", () -> wrapProxy(rawA)); // 三级缓存放 ObjectFactory
            ServiceB b = new ServiceB();
            b.a = (ServiceA) getBean("a", l1, l2, l3); // 触发提前代理并升级到二级缓存
            rawA.b = b;
            Object finalA = l2.containsKey("a") ? l2.remove("a") : wrapProxy(rawA);
            l1.put("a", finalA);
            if (b.a.isProxy() && b.a == l1.get("a")) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("proxy_injected=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
