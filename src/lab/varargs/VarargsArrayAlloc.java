package lab.varargs;

public class VarargsArrayAlloc {
    static int sum(int... nums) {
        int s = 0;
        for (int x : nums) s += x;
        return s;
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            if (sum(1, 2) == 3) ok++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
