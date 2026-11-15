package lab.sqljoin;

import java.util.HashMap;
import java.util.Map;

public class HashJoin {
    static final int USERS = 10_000;

    static void networkRtt() {
        long until = System.nanoTime() + 15_000L;
        while (System.nanoTime() < until) {
            Thread.onSpinWait();
        }
    }

    public static void main(String[] args) {
        int orderCount = Integer.parseInt(args[0]);
        String[] userTable = new String[USERS];
        for (int i = 0; i < USERS; i++) userTable[i] = "user_" + i;

        int[] orderUserIds = new int[orderCount];
        for (int i = 0; i < orderCount; i++) orderUserIds[i] = (i * 37) % USERS;

        long start = System.nanoTime();
        int roundTrips = 0;

        // Single SQL JOIN: DB builds hash table on smaller side and probes in one round-trip
        networkRtt();
        roundTrips++;
        Map<Integer, String> hashTable = new HashMap<>(USERS * 2);
        for (int i = 0; i < USERS; i++) hashTable.put(i, userTable[i]);

        int matched = 0;
        for (int uid : orderUserIds) {
            if (hashTable.get(uid) != null) matched++;
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("roundTrips=" + roundTrips);
        System.out.println("expected=1");
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
