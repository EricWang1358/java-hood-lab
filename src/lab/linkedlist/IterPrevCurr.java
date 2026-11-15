package lab.linkedlist;

public class IterPrevCurr {
    static class Node {
        int val;
        Node next;
        Node(int val) { this.val = val; }
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
        Node prev = null, curr = head;
        while (curr != null) {
            Node next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("headVal=" + prev.val);
        System.out.println("expected=" + (n - 1));
        System.out.println("ms=" + ms);
    }
}
