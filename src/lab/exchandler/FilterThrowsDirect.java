package lab.exchandler;

public class FilterThrowsDirect {
    static String handleInFilter() {
        try {
            throw new SecurityException("Token 签名过期");
        } catch (RuntimeException e) {
            // Filter 在 DispatcherServlet 之外，@RestControllerAdvice 抓不到！容器直接吐默认 500 HTML
            return "500 HTML";
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            if (handleInFilter().equals("JSON")) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("unified_json=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
