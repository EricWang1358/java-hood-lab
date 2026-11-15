package lab.strimmut;

public class LoopPlusConcat {
    static volatile int sink;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        String s = "";
        for (int i = 0; i < n; i++) {
            s = s + "a";
        }
        sink = s.length();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lenOk=" + s.length());
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
