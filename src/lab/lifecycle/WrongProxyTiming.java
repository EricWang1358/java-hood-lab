package lab.lifecycle;

import java.lang.reflect.Proxy;

public class WrongProxyTiming {
    interface WarmupService { boolean warmupWithTx(); }
    static class WarmupServiceImpl implements WarmupService {
        WarmupService self;
        boolean txActive = false;
        boolean warmedWithTx = false;

        void postConstruct() {
            // @PostConstruct 时 AOP 代理还没包装，self 仍是裸对象或 null
            warmedWithTx = self != null && self.warmupWithTx();
        }

        @Override
        public boolean warmupWithTx() { return txActive; }
    }

    static WarmupService wrapProxy(WarmupServiceImpl target) {
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
            raw.self = raw; // 注入时还没经过 BPP.after
            raw.postConstruct(); // 此时还没生成代理
            wrapProxy(raw); // BPP.after 在 @PostConstruct 之后才生成代理
            if (raw.warmedWithTx) ready++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ready=" + ready);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
