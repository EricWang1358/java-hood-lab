package lab.bintree;

public class MorrisTraversal {
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
        TreeNode curr = root;
        while (curr != null) {
            if (curr.left == null) {
                visited++;
                curr = curr.right;
            } else {
                TreeNode pre = curr.left;
                while (pre.right != null && pre.right != curr) pre = pre.right;
                if (pre.right == null) {
                    pre.right = curr;
                    visited++;
                    curr = curr.left;
                } else {
                    pre.right = null;
                    curr = curr.right;
                }
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("visited=" + visited);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
