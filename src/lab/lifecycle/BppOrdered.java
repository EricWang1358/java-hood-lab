package lab.lifecycle;

import java.lang.reflect.Proxy;

public class BppOrdered {
    interface UserService { int getCount(); }
    static class Repo { int count() { return 42; } }

    static class UserServiceImpl implements UserService {
        Repo repo;
        int cachedCount;

        void postConstruct() { cachedCount = repo.count(); }
        @Override public int getCount() { return cachedCount; }
    }

    static UserService bppAfter(UserServiceImpl target) {
        return (UserService) Proxy.newProxyInstance(
            UserService.class.getClassLoader(),
            new Class<?>[]{UserService.class},
            (p, m, a) -> m.invoke(target, a)
        );
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ready = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            // 1. 实例化 -> 2. 属性注入 -> 3. @PostConstruct -> 4. BPP.after 生成代理
            UserServiceImpl raw = new UserServiceImpl();
            raw.repo = new Repo();
            raw.postConstruct();
            UserService proxy = bppAfter(raw);
            if (proxy.getCount() == 42) ready++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ready=" + ready);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
