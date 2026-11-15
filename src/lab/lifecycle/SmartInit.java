package lab.lifecycle;

import java.lang.reflect.Proxy;

public class SmartInit {
    interface WarmupService { boolean warmupWithTx(); }
    static class WarmupServiceImpl implements WarmupService {
        boolean txActive = false;
        @Override
        public boolean warmupWithTx() { return txActive; }
    }

    static WarmupService bppAfter(WarmupServiceImpl target) {
        return (WarmupService) Proxy.newProxyInstance(
            WarmupService.class.getClassLoader(),
            new Class<?>[]{WarmupService.class},
            (p, m, a) -> {
                target.txActive = true;
                try { return m.invoke(target, a); }
                finally { target.txActive = false; }
            }
        );
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ready = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            WarmupServiceImpl raw = new WarmupServiceImpl();
            WarmupService proxy = bppAfter(raw);
            // ApplicationReadyEvent 在所有 Bean 与 AOP 代理就绪后触发
            if (proxy.warmupWithTx()) ready++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ready=" + ready);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
