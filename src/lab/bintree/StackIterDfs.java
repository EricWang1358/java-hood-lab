package lab.bintree;

import java.util.ArrayDeque;
import java.util.Deque;

public class StackIterDfs {
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
        Deque<TreeNode> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            TreeNode node = stack.pop();
            visited++;
            if (node.right != null) stack.push(node.right);
            if (node.left != null) stack.push(node.left);
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("visited=" + visited);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
