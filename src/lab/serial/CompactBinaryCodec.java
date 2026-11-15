package lab.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CompactBinaryCodec {
    public static void main(String[] args) throws Exception {
        int n = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int ok = 0;
        for (int i = 0; i < n; i++) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(32);
            try (DataOutputStream dos = new DataOutputStream(bos)) {
                dos.writeLong(i);
                dos.writeUTF("ORD-2025");
            }
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
                long id = dis.readLong();
                String code = dis.readUTF();
                if (id == i && "ORD-2025".equals(code)) ok++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + ok);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
