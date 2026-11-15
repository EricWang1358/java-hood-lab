package lab.stringtable;

public class StringBuilderReuse {
    static volatile String sink;

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        String[] canonical = new String[100];
        for (int i = 0; i < 100; i++) canonical[i] = ("key-" + i).intern();

        long start = System.nanoTime();
        StringBuilder sb = new StringBuilder(16);
        int dedupOk = 0;
        for (int i = 0; i < n; i++) {
            int idx = i % 100;
            sb.setLength(0);
            sb.append("key-").append(idx);
            String s = sb.toString().intern();
            if (s == canonical[idx]) dedupOk++;
            sink = s;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("dedupOk=" + dedupOk);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
