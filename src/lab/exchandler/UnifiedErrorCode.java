package lab.exchandler;

public class UnifiedErrorCode {
    static class BizException extends RuntimeException {
        final int code;
        BizException(int code, String msg) { super(msg); this.code = code; }
    }

    static String adviceHandle(RuntimeException e) {
        if (e instanceof BizException) return "JSON";
        return "JSON";
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                throw new BizException(4001, "库存不足");
            } catch (RuntimeException e) {
                if (adviceHandle(e).equals("JSON")) ok++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("unified_json=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
