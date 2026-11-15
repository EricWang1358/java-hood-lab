package lab.exchandler;

public class CatchInController {
    static String controllerCatch() {
        try {
            throw new IllegalStateException("库存不足");
        } catch (Exception e) {
            // 到处手写 try-catch 返回裸字符串或吞掉错误码，格式不统一
            return "error: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            if (controllerCatch().equals("JSON")) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("unified_json=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
