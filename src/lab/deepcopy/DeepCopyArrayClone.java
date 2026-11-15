package lab.deepcopy;

public class DeepCopyArrayClone {
    static class User {
        int[] scores;
        User(int[] scores) { this.scores = scores; }
        User deepCopy() {
            return new User(this.scores.clone());
        }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int isolated = 0;
        for (int i = 0; i < n; i++) {
            User origin = new User(new int[]{100, 90});
            User copy = origin.deepCopy();
            copy.scores[0] = 0;
            if (origin.scores[0] == 100) isolated++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("isolated=" + isolated);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
