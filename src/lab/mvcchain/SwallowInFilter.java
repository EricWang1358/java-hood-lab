package lab.mvcchain;

public class SwallowInFilter {
    static boolean filter(boolean forgetChain) {
        if (forgetChain) return false; // 忘了调用 chain.doFilter()，请求直接断在半路
        return true;
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            if (filter(true)) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("protected_ok=" + ok);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
