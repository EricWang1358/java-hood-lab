package lab.clinit;

public class OrderTrapInit {
    static class BadConfig {
        static BadConfig INSTANCE = new BadConfig();
        static int BASE = 100;
        final int effective;

        BadConfig() {
            this.effective = BASE + 1;
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            if (BadConfig.INSTANCE.effective == 101) {
                ok++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
