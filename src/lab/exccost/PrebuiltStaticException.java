package lab.exccost;

public class PrebuiltStaticException {
    static final IllegalArgumentException SHARED = new IllegalArgumentException("bad input");

    static int depthCall(int d) {
        if (d > 0) return depthCall(d - 1);
        throw SHARED;
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int handled = 0;
        for (int i = 0; i < n; i++) {
            try {
                depthCall(8);
            } catch (IllegalArgumentException e) {
                handled++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("handled=" + handled);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
