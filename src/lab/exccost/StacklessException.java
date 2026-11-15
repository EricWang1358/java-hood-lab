package lab.exccost;

public class StacklessException {
    static final class FastBizException extends RuntimeException {
        FastBizException(String msg) { super(msg, null, false, false); }
    }

    static int depthCall(int d) {
        if (d > 0) return depthCall(d - 1);
        throw new FastBizException("bad input");
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int handled = 0;
        for (int i = 0; i < n; i++) {
            try {
                depthCall(8);
            } catch (FastBizException e) {
                handled++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("handled=" + handled);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
