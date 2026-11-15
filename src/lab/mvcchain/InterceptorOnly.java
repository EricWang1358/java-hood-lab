package lab.mvcchain;

public class InterceptorOnly {
    static boolean isStaticOrFilterError(int i) { return i % 2 == 0; }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            // 只配了 Interceptor，进 DispatcherServlet 之前的静态资源/CORS 预检请求绕过了 Interceptor
            if (!isStaticOrFilterError(i)) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("protected_ok=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
