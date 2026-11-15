package lab.pagecache;

import java.io.File;
import java.io.RandomAccessFile;

public class RandomSeekWrite {
    public static void main(String[] args) throws Exception {
        int records = Integer.parseInt(args[0]);
        byte[] block = new byte[1024];
        File tmp = File.createTempFile("pc-rand-", ".dat");
        tmp.deleteOnExit();

        long start = System.nanoTime();
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.setLength((long) records * 1024);
            for (int i = 0; i < records; i++) {
                long offset = (long) ((i * 73) % records) * 1024;
                raf.seek(offset);
                raf.write(block);
            }
            raf.getChannel().force(true);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("written=" + records);
        System.out.println("expected=" + records);
        System.out.println("ms=" + ms);
    }
}
