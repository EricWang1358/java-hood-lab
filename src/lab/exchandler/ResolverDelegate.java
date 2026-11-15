package lab.exchandler;

public class ResolverDelegate {
    static String globalAdvice(RuntimeException e) {
        return "JSON";
    }

    static String filterWithResolver() {
        try {
            throw new SecurityException("Token 签名过期");
        } catch (RuntimeException e) {
            // Filter 里注入 HandlerExceptionResolver，把异常转交给 @RestControllerAdvice
            return globalAdvice(e);
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            if (filterWithResolver().equals("JSON")) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("unified_json=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
