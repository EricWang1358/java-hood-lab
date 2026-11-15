package lab.walredo;

import java.util.Arrays;

public class RedoPlusUndo {
    public static void main(String[] args) {
        int ops = Integer.parseInt(args[0]);
        long start = System.nanoTime();
        int[] bufferPool = new int[64];
        Arrays.fill(bufferPool, 100);
        int[] redoLog = new int[ops];
        int[] undoLog = new int[ops];
        int lostOrDirty = 0;

        for (int i = 0; i < ops; i++) {
            int rowId = i & 63;
            int oldVal = bufferPool[rowId];
            undoLog[i] = oldVal;          // Save before-image to Undo Log
            int newVal = oldVal + 50;
            bufferPool[rowId] = newVal;   // Modify Buffer Pool
            redoLog[i] = newVal;          // Append sequential Redo Log
            boolean rollback = (i % 5 == 0);
            if (rollback) {
                bufferPool[rowId] = undoLog[i]; // Undo restores exact old image
                if (bufferPool[rowId] != oldVal) lostOrDirty++;
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("lostOrDirty=" + lostOrDirty);
        System.out.println("expected=0");
        System.out.println("ms=" + ms);
    }
}
