package lab.deepcopy;

public class ShallowCloneTrap {
    static class User implements Cloneable {
        int[] scores;
        User(int[] scores) { this.scores = scores; }

        @Override
        public User clone() {
            try {
                return (User) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int isolated = 0;
        for (int i = 0; i < n; i++) {
            User origin = new User(new int[]{100, 90});
            User copy = origin.clone();
            copy.scores[0] = 0;
            if (origin.scores[0] == 100) isolated++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("isolated=" + isolated);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
