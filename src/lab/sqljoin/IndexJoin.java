package lab.sqljoin;

public class IndexJoin {
    static final int USERS = 10_000;

    static void networkRtt() {
        long until = System.nanoTime() + 15_000L;
        while (System.nanoTime() < until) {
            Thread.onSpinWait();
        }
    }

    public static void main(String[] args) {
        int orderCount = Integer.parseInt(args[0]);
        String[] userPkIndex = new String[USERS];
        for (int i = 0; i < USERS; i++) userPkIndex[i] = "user_" + i;

        int[] orderUserIds = new int[orderCount];
        for (int i = 0; i < orderCount; i++) orderUserIds[i] = (i * 37) % USERS;

        long start = System.nanoTime();
        int roundTrips = 0;

        // Single SQL Index Nested-Loop Join: 1 RTT, direct PK lookup inside DB
        networkRtt();
        roundTrips++;
        int matched = 0;
        for (int uid : orderUserIds) {
            if (userPkIndex[uid] != null) matched++;
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("roundTrips=" + roundTrips);
        System.out.println("expected=1");
        System.out.println("matched=" + matched);
        System.out.println("ms=" + ms);
    }
}
