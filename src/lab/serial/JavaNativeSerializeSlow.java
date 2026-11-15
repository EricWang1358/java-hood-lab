package lab.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class JavaNativeSerializeSlow {
    static class OrderDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        long id;
        String code;
        transient String secretToken;
        OrderDTO(long id, String code, String secretToken) {
            this.id = id;
            this.code = code;
            this.secretToken = secretToken;
        }
    }

    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            OrderDTO dto = new OrderDTO(i, "ORD-2025", "tok-999");
            ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
            try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(dto);
            }
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
                OrderDTO out = (OrderDTO) ois.readObject();
                if (out.id == i && out.secretToken == null) ok++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
