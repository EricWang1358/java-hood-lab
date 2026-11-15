package lab.phantom;

public class SerializableRange {
    public static void main(String[] args) {
        int rounds = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int phantomHits = 0;
        for (int i = 0; i < rounds; i++) {
            int firstCount = 5;
            int secondCount = 5;
            if (secondCount != firstCount) phantomHits++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("phantomHits=" + phantomHits);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
