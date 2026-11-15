package lab.bintree;

public class RecursiveDfs {
    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    static int countNodes(TreeNode root) {
        if (root == null) return 0;
        return 1 + countNodes(root.left) + countNodes(root.right);
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
        int visited = -1;
        try {
            visited = countNodes(root);
        } catch (StackOverflowError e) {
            visited = -1;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("visited=" + visited);
        System.out.println("expected=" + n);
        System.out.println("ms=" + ms);
    }
}
