package lab.passbyval;

public class MutateFieldInPlace {
    static class Box {
        int val;
        Box(int val) { this.val = val; }
    }

    static void mutate(Box b, int target) {
        b.val = target;
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int updated = 0;
        for (int i = 0; i < n; i++) {
            Box box = new Box(0);
            mutate(box, 99);
            if (box.val == 99) updated++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("updated=" + updated);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
