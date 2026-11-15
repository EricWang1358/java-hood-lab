package lab.cpudiag;

import java.util.regex.Pattern;

public class RegexBacktrackReDoS {
    static final Pattern EVIL = Pattern.compile("(a+)+b");

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        String input = "aaaaaaaaaaaaaaaaaac";
        int ok = 0;
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            if (!EVIL.matcher(input).matches()) {
                ok++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
