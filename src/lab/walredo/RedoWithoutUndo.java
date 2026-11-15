package lab.walredo;

import java.util.Arrays;

public class RedoWithoutUndo {
    public static void main(String[] args) {
        int ops = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int[] bufferPool = new int[64];
        Arrays.fill(bufferPool, 100);
        int[] redoLog = new int[ops];
        int lostOrDirty = 0;

        for (int i = 0; i < ops; i++) {
            int rowId = i & 63;
            int expectedBefore = bufferPool[rowId];
            int newVal = expectedBefore + 50;
            bufferPool[rowId] = newVal;   // Update memory page directly
            redoLog[i] = newVal;          // Append physical Redo record
            boolean rollback = (i % 5 == 0);
            if (rollback) {
                // No Undo Log saved! We cannot restore expectedBefore!
                if (bufferPool[rowId] != expectedBefore) lostOrDirty++;
                bufferPool[rowId] = expectedBefore; // reset for next round
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lostOrDirty=" + lostOrDirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
