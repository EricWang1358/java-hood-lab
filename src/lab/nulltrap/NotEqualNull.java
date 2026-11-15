package lab.nulltrap;

public class NotEqualNull {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        String[] status = new String[rows];
        for (int i = 0; i < rows; i++) {
            if (i % 10 == 0) status[i] = "CANCELLED";
            else if (i % 5 == 0) status[i] = null; // unpaid / pending NULL
            else status[i] = "PAID";
        }

        long start = System.nanoTime();
        int returned = 0;
        for (int i = 0; i < rows; i++) {
            // WHERE status != 'CANCELLED': NULL != 'CANCELLED' is UNKNOWN, so NULL rows vanish!
            if (status[i] != null && !status[i].equals("CANCELLED")) {
                returned++;
            }
        }

        int expectedNonCancelled = rows - (rows + 9) / 10;
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("returned=" + returned);
        System.out.println("expected=" + expectedNonCancelled);
        System.out.println("ms=" + ms);
    }
}
