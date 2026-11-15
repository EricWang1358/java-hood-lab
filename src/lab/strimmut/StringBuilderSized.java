package lab.strimmut;

public class StringBuilderSized {
    static volatile int sink;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append("a");
        }
        String s = sb.toString();
        sink = s.length();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lenOk=" + s.length());
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
