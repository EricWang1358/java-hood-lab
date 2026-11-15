package lab.linkedlist;

import java.util.ArrayList;
import java.util.List;

public class ArrayCopy {
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
        List<Node> list = new ArrayList<>(n);
        for (Node p = head; p != null; p = p.next) list.add(p);
        for (int i = list.size() - 1; i > 0; i--) {
            list.get(i).next = list.get(i - 1);
        }
        list.get(0).next = null;
        Node newHead = list.get(list.size() - 1);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("headVal=" + newHead.val);
        System.out.println("expected=" + (n - 1));
        System.out.println("ms=" + ms);
    }
}
