package lab.idxfunc;

public class ImplicitCast {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        String[] phones = new String[n];
        for (int i = 0; i < n; i++) phones[i] = String.valueOf(13800000000L + i);
        long start = System.nanoTime();
        int scanned = 0;
        int matched = 0;
        // WHERE phone = 13800000050 (int literal on varchar col): MySQL wraps CAST(phone AS DOUBLE) -> full scan!
        for (int i = 0; i < n; i++) {
            scanned++;
            if (Long.parseLong(phones[i]) == 13800000050L) matched++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("scanned=" + scanned);
        System.out.println("expected=50");
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
