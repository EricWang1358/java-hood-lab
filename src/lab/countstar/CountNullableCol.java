package lab.countstar;

public class CountNullableCol {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        String[] remarks = new String[rows];
        for (int i = 0; i < rows; i++) {
            remarks[i] = (i % 10 == 0) ? null : "ok";
        }

        long start = System.nanoTime();
        int counted = 0;
        // COUNT(remark): must read column value and skip NULLs
        for (int i = 0; i < rows; i++) {
            if (remarks[i] != null) counted++;
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("counted=" + counted);
        System.out.println("expected=" + rows);
        System.out.println("ms=" + ms);
    }
}
