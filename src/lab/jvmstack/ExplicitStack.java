package lab.jvmstack;

import java.util.ArrayDeque;

public class ExplicitStack {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        ArrayDeque<Integer> stack = new ArrayDeque<>(Math.min(n, 1024));
        for (int i = 1; i <= n; i++) {
            stack.push(i);
        }
        long s = 0;
        int reached = 0;
        while (!stack.isEmpty()) {
            s += stack.pop();
            reached++;
        }
        if (s < 0) System.out.print("");
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("reached=" + reached);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
