package lab.linkedlist;

public class RecursiveRev {
    static class Node {
        int val;
        Node next;
        Node(int val) { this.val = val; }
    }

    static Node reverse(Node head) {
        if (head == null || head.next == null) return head;
        Node p = reverse(head.next);
        head.next.next = head;
        head.next = null;
        return p;
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        Node head = new Node(0);
        Node cur = head;
        for (int i = 1; i < n; i++) {
            cur.next = new Node(i);
            cur = cur.next;
        }
        long start = System.nanoTime();
        int headVal = -1;
        try {
            Node newHead = reverse(head);
            headVal = newHead.val;
        } catch (StackOverflowError e) {
            headVal = -1;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("headVal=" + headVal);
        System.out.println("expected=" + (n - 1));
        System.out.println("ms=" + ms);
    }
}
