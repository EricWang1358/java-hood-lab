package lab.deepcopy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializeDeepCopy {
    static class User implements Serializable {
        int[] scores;
        User(int[] scores) { this.scores = scores; }

        User serializeCopy() throws Exception {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(this);
            }
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
                return (User) ois.readObject();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int isolated = 0;
        for (int i = 0; i < n; i++) {
            User origin = new User(new int[]{100, 90});
            User copy = origin.serializeCopy();
            copy.scores[0] = 0;
            if (origin.scores[0] == 100) isolated++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("isolated=" + isolated);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
