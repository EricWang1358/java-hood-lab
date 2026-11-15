package lab.altprint;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionAlt {
    static int state = 0;

    public static void main(String[] args) throws Exception {
        int rounds = Integer.parseInt(args[0]);
        int[] out = new int[rounds * 3];
        ReentrantLock lock = new ReentrantLock();
        Condition[] conds = {lock.newCondition(), lock.newCondition(), lock.newCondition()};
        long start = System.nanoTime();
        Thread[] ts = new Thread[3];
        for (int id = 0; id < 3; id++) {
            final int myId = id;
            ts[id] = new Thread(() -> {
                for (int r = 0; r < rounds; r++) {
                    lock.lock();
                    try {
                        while (state % 3 != myId) conds[myId].await();
                        out[state] = myId;
                        state++;
                        conds[(myId + 1) % 3].signal();
                    } catch (InterruptedException ignored) {
                    } finally {
                        lock.unlock();
                    }
                }
            });
            ts[id].start();
        }
        for (Thread t : ts) t.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        int ok = 1;
        for (int i = 0; i < out.length; i++) {
            if (out[i] != i % 3) { ok = 0; break; }
        }
        System.out.println("ok=" + ok);
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
