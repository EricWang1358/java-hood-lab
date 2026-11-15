package lab.bintree;

import java.util.ArrayDeque;
import java.util.Deque;

public class BfsLevel {
    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        TreeNode root = new TreeNode(0);
        TreeNode cur = root;
        for (int i = 1; i < n; i++) {
            cur.right = new TreeNode(i);
            cur = cur.right;
        }
        long start = System.nanoTime();
        int visited = 0;
        Deque<TreeNode> queue = new ArrayDeque<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                visited++;
                if (node.left != null) queue.offer(node.left);
                if (node.right != null) queue.offer(node.right);
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("visited=" + visited);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
