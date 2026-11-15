package lab.mvcchain;

public class FilterAndInterceptor {
    static boolean runFilterChain(Runnable next) {
        // 1. Filter 负责全局 CORS / TraceId / 请求日志（DispatcherServlet 之外）
        next.run();
        return true;
    }

    static boolean runInterceptor(Runnable controller) {
        // 2. HandlerInterceptor 负责登录态与 Spring Bean 权限校验
        controller.run();
        return true;
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int[] ok = {0};
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            runFilterChain(() -> runInterceptor(() -> ok[0]++));
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("protected_ok=" + ok[0]);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
