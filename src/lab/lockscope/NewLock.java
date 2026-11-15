package lab.lockscope;

public class NewLock {
    static final int ROUNDS = 500_000;
    static int total = 0;

    synchronized void add() {
        total++;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                for (int i = 0; i < ROUNDS; i++) {
                    new NewLock().add();
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("total=" + total);
        System.out.println("expected=" + (threads * ROUNDS));
        System.out.println("ms=" + ms);
    }
}
