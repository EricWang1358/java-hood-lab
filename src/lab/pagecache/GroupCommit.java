package lab.pagecache;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class GroupCommit {
    public static void main(String[] args) throws Exception {
        int records = Integer.parseInt(args[0]);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        File tmp = File.createTempFile("pc-grp-", ".dat");
        tmp.deleteOnExit();

        long start = System.nanoTime();
        try (FileChannel fc = FileChannel.open(tmp.toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 0; i < records; i++) {
                buf.clear();
                while (buf.hasRemaining()) fc.write(buf);
                if ((i + 1) % 50 == 0) {
                    fc.force(false);
                }
            }
            fc.force(false);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("written=" + records);
        System.out.println("expected=" + records);
        System.out.println("ms=" + ms);
    }
}
