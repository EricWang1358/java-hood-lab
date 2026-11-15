package lab.serial;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

public class MissingSerializableCrash {
    static class PlainDTO {
        long id = 1L;
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
                oos.writeObject(new PlainDTO());
                ok++;
            } catch (NotSerializableException ignored) {
            } catch (Exception ignored) {
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
