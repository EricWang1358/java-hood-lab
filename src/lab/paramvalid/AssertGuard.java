package lab.paramvalid;

public class AssertGuard {
    record OrderCmd(int amount) {
        OrderCmd {
            if (amount <= 0) throw new IllegalArgumentException("Domain Guard");
        }
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int rejected = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                new OrderCmd(-100);
            } catch (IllegalArgumentException e) {
                rejected++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("rejected=" + rejected);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
