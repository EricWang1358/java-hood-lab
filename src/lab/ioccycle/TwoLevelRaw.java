package lab.ioccycle;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class TwoLevelRaw {
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

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            Map<String, Object> l1 = new HashMap<>();
            Map<String, Object> l2 = new HashMap<>();
            ServiceAImpl rawA = new ServiceAImpl();
            // 只有二级缓存：直接暴露原始对象 rawA
            l2.put("a", rawA);
            ServiceB b = new ServiceB();
            b.a = (ServiceA) l2.get("a"); // B 拿到的是未代理的 rawA
            rawA.b = b;
            l1.put("a", wrapProxy(rawA)); // 一级缓存后来放了代理对象，B 拿的却还是 rawA
            if (b.a.isProxy() && b.a == l1.get("a")) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("proxy_injected=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
