package lab.jsontrap;

public class FloatDoubleMoney {
    public static void main(String[] args) {
        int total = Integer.parseInt(args[0]) * 1000;
        int exact = 0;
        long start = System.nanoTime();
        for (int i = 0; i < total; i++) {
            double totalYuan = 0.1 + 0.2; // 0.30000000000000004
            if (totalYuan == 0.3) exact++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("exact=" + exact);
        System.out.println("expected=" + total);
        System.out.println("ms=" + ms);
    }
}
