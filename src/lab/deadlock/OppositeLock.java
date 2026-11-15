package lab.deadlock;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class OppositeLock {
    static final int WAVES = 150;
    static final int TRANSFERS = 50;
    static final int INIT = 1000;
    static Account[] accounts;
    static volatile int audit;
    static final AtomicBoolean deadlocked = new AtomicBoolean();
    static final AtomicInteger okWaves = new AtomicInteger();

    static class Account {
        final int id;
        int balance = INIT;
        final ReentrantLock lock = new ReentrantLock();
        Account(int id) { this.id = id; }
    }

    static Account[] newAccounts() {
        Account[] arr = new Account[8];
        for (int i = 0; i < arr.length; i++) arr[i] = new Account(i);
        return arr;
    }

    static void recordLog(int a, int b) {
        int h = a ^ b;
        for (int k = 0; k < 2000; k++) h = h * 31 + k;
        audit = h;
    }

    static void transfer(Account from, Account to, int amount) throws InterruptedException {
        from.lock.lock();
        try {
            if (!to.lock.tryLock(30, TimeUnit.MICROSECONDS)) {
                deadlocked.set(true);
                return;
            }
            try {
                int a = from.balance - amount;
                int b = to.balance + amount;
                recordLog(a, b);
                from.balance = a;
                to.balance = b;
            } finally {
                to.lock.unlock();
            }
        } finally {
            from.lock.unlock();
        }
    }

    static boolean isConserved() {
        if (deadlocked.get()) return false;
        int sum = 0;
        for (Account a : accounts) sum += a.balance;
        return sum == accounts.length * INIT;
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        accounts = newAccounts();
        AtomicInteger wave = new AtomicInteger();
        CyclicBarrier barrier = new CyclicBarrier(threads, () -> {
            if (wave.getAndIncrement() > 0) {
                if (isConserved()) okWaves.incrementAndGet();
                deadlocked.set(false);
                accounts = newAccounts();
            }
        });
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            int pair = (t / 2) * 2;
            boolean rev = (t % 2) == 1;
            workers[t] = new Thread(() -> {
                try {
                    for (int w = 0; w < WAVES; w++) {
                        barrier.await();
                        Account a = accounts[rev ? pair + 1 : pair];
                        Account b = accounts[rev ? pair : pair + 1];
                        for (int i = 0; i < TRANSFERS && !deadlocked.get(); i++) {
                            transfer(a, b, 1);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        if (isConserved()) okWaves.incrementAndGet();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("okWaves=" + okWaves.get());
        System.out.println("expected=" + WAVES);
        System.out.println("ms=" + ms);
    }
}
