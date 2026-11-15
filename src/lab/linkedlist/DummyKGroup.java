package lab.linkedlist;

public class DummyKGroup {
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
        Node dummy = new Node(-1);
        dummy.next = head;
        Node prev = dummy;
        Node curr = head;
        while (curr != null && curr.next != null) {
            Node next = curr.next;
            curr.next = next.next;
            next.next = prev.next;
            prev.next = next;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("headVal=" + dummy.next.val);
        System.out.println("expected=" + (n - 1));
        System.out.println("ms=" + ms);
    }
}
