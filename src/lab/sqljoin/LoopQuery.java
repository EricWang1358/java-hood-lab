package lab.sqljoin;

import java.util.HashMap;
import java.util.Map;

public class LoopQuery {
    static final int USERS = 10_000;

    static void networkRtt() {
        long until = System.nanoTime() + 15_000L; // ~0.015ms per local SQL round-trip
        while (System.nanoTime() < until) {
            Thread.onSpinWait();
        }
    }

    public static void main(String[] args) {
        int orderCount = Integer.parseInt(args[0]);
        Map<Integer, String> userTable = new HashMap<>(USERS * 2);
        for (int i = 0; i < USERS; i++) userTable.put(i, "user_" + i);

        int[] orderUserIds = new int[orderCount];
        for (int i = 0; i < orderCount; i++) orderUserIds[i] = (i * 37) % USERS;

        long start = System.nanoTime();
        int roundTrips = 0;
        int matched = 0;

        // 1st query: fetch N orders
        networkRtt();
        roundTrips++;

        // N queries: fetch user one by one in a loop
        for (int uid : orderUserIds) {
            networkRtt();
            roundTrips++;
            if (userTable.get(uid) != null) matched++;
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("roundTrips=" + roundTrips);
        System.out.println("expected=1");
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
