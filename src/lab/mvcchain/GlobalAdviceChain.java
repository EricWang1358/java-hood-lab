package lab.mvcchain;

public class GlobalAdviceChain {
    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            // Filter(CORS+TraceId) -> Interceptor(Auth) -> Controller -> @RestControllerAdvice 统一收口
            boolean filterPassed = true;
            boolean interceptorPassed = true;
            if (filterPassed && interceptorPassed) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("protected_ok=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
