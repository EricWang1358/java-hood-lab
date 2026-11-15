package lab.paramvalid;

public class NoValidAnnotation {
    static boolean saveOrder(int amount, boolean hasValid) {
        if (hasValid && amount <= 0) throw new IllegalArgumentException("金额必须大于0");
        return false; // 忘了写 @Valid，负数金额直接进了业务
    }

    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int rejected = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            try {
                if (saveOrder(-100, false)) rejected++;
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
