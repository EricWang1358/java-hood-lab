package lab.tslru;

import java.util.HashMap;
import java.util.Map;

public class SyncNodeLru {
    static class Node {
        int k, v;
        Node prev, next;
        Node(int k, int v) { this.k = k; this.v = v; }
    }

    static class Lru {
        private final int cap;
        private final Map<Integer, Node> map = new HashMap<>();
        private final Node head = new Node(0, 0), tail = new Node(0, 0);

        Lru(int cap) {
            this.cap = cap;
            head.next = tail;
            tail.prev = head;
        }

        synchronized int get(int k) {
            Node n = map.get(k);
            if (n == null) return -1;
            moveToHead(n);
            return n.v;
        }

        synchronized void put(int k, int v) {
            Node n = map.get(k);
            if (n != null) {
                n.v = v;
                moveToHead(n);
                return;
            }
            Node created = new Node(k, v);
            map.put(k, created);
            addFirst(created);
            if (map.size() > cap) {
                Node last = tail.prev;
                remove(last);
                map.remove(last.k);
            }
        }

        synchronized int size() { return map.size(); }

        private void moveToHead(Node n) { remove(n); addFirst(n); }
        private void addFirst(Node n) {
            n.next = head.next;
            n.prev = head;
            head.next.prev = n;
            head.next = n;
        }
        private void remove(Node n) {
            n.prev.next = n.next;
            n.next.prev = n.prev;
        }
    }

    public static void main(String[] args) throws Exception {
        int threads = Integer.parseInt(args[0]);
        int ops = 100_000;
        Lru lru = new Lru(64);
        for (int i = 0; i < 64; i++) lru.put(i, i);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                for (int i = 0; i < ops; i++) {
                    if ((i & 7) == 0) lru.put(i & 127, i);
                    else lru.get(i & 127);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("ok=" + (lru.size() <= 64 ? 1 : 0));
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
