package lab.nulltrap;

import java.util.HashSet;
import java.util.Set;

public class NotExistsSafe {
    public static void main(String[] args) {
        int rows = Integer.parseInt(args[0]);
        Integer[] blacklist = new Integer[]{1, 2, null};
        Set<Integer> nonNullSet = new HashSet<>();
        for (Integer v : blacklist) {
            if (v != null) nonNullSet.add(v);
        }

        long start = System.nanoTime();
        int returned = 0;
        for (int id = 1; id <= rows; id++) {
            // NOT EXISTS (SELECT 1 FROM ... WHERE id = u.id) ignores NULLs safely
            if (!nonNullSet.contains(id)) {
                returned++;
            }
        }

        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("returned=" + returned);
        System.out.println("expected=" + (rows - 2));
        System.out.println("ms=" + ms);
    }
}
