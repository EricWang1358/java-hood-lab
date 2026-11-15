package lab.lifecycle;

public class InitBeforePopulate {
    static class Repo { int count() { return 42; } }

    static class UserService {
        Repo repo;
        int cachedCount;

        void postConstruct() {
            cachedCount = repo.count();
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ready = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                UserService bean = new UserService();
                // 错误顺序：在属性注入之前就调用了初始化
                bean.postConstruct();
                bean.repo = new Repo();
                if (bean.cachedCount == 42) ready++;
            } catch (NullPointerException ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ready=" + ready);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
