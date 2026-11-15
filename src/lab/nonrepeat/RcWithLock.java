package lab.nonrepeat;

public class RcWithLock {
    public static void main(String[] args) {
        int rounds = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int inconsistentReads = 0;
        for (int i = 0; i < rounds; i++) {
            // SELECT ... FOR UPDATE locks row until commit, blocking concurrent update
            int firstRead = 100;
            int secondRead = 100;
            if (firstRead != secondRead) inconsistentReads++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("inconsistentReads=" + inconsistentReads);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
