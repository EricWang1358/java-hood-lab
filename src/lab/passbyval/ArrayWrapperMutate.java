package lab.passbyval;

public class ArrayWrapperMutate {
    static void updateHolder(int[] holder, int target) {
        holder[0] = target;
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int updated = 0;
        for (int i = 0; i < n; i++) {
            int[] holder = new int[]{0};
            updateHolder(holder, 99);
            if (holder[0] == 99) updated++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("updated=" + updated);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
