package lab.nulltrap;

public class NotInWithNull {
    enum Tri { TRUE, FALSE, UNKNOWN }

    static Tri sqlNotIn(int id, Integer[] listWithNull) {
        boolean sawUnknown = false;
        for (Integer v : listWithNull) {
            if (v == null) {
                sawUnknown = true;
            } else if (v == id) {
                return Tri.FALSE;
            }
        }
        return sawUnknown ? Tri.UNKNOWN : Tri.TRUE;
    }

    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        // Subquery returns [1, 2, null]
        Integer[] blacklist = new Integer[]{1, 2, null};

        long start = System.nanoTime();
        int returned = 0;
        for (int id = 1; id <= rows; id++) {
            // WHERE only keeps rows evaluating to TRUE; UNKNOWN is dropped!
            if (sqlNotIn(id, blacklist) == Tri.TRUE) {
                returned++;
            }
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("returned=" + returned);
        System.out.println("expected=" + (rows - 2));
        System.out.println("ms=" + ms);
    }
}
