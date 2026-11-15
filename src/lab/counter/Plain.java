package lab.counter;

public class Plain {
    static int count = 0;

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int times = 1_000_000;
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();

        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int n = 0; n < times; n++) {
                    count++;
                }
            });
            workers[i].start();
        }
        for (Thread worker : workers) {
            worker.join();
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("count=" + count);
        System.out.println("expected=" + threads * times);
        System.out.println("ms=" + ms);
    }
}
