package lab.tslru;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SegmentedLru {
    static class Node {
        int k, v;
        Node prev, next;
        Node(int k, int v) { this.k = k; this.v = v; }
    }

    static class Shard {
        final ReentrantLock lock = new ReentrantLock();
        final int cap;
        final Map<Integer, Node> map = new HashMap<>();
        final Node head = new Node(0, 0), tail = new Node(0, 0);

        Shard(int cap) {
            this.cap = cap;
            head.next = tail;
            tail.prev = head;
        }

        int get(int k) {
            lock.lock();
            try {
                Node n = map.get(k);
                if (n == null) return -1;
                remove(n);
                addFirst(n);
                return n.v;
            } finally {
                lock.unlock();
            }
        }

        void put(int k, int v) {
            lock.lock();
            try {
                Node n = map.get(k);
                if (n != null) {
                    n.v = v;
                    remove(n);
                    addFirst(n);
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
            } finally {
                lock.unlock();
            }
        }

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
        Shard[] shards = new Shard[16];
        for (int i = 0; i < 16; i++) shards[i] = new Shard(4);
        for (int i = 0; i < 64; i++) shards[i & 15].put(i, i);
        Thread[] workers = new Thread[threads];
        long start = System.nanoTime();
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                for (int i = 0; i < ops; i++) {
                    int k = i & 127;
                    Shard s = shards[k & 15];
                    if ((i & 7) == 0) s.put(k, i);
                    else s.get(k);
                }
            });
            workers[t].start();
        }
        for (Thread w : workers) w.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        int total = 0;
        for (Shard s : shards) total += s.map.size();
        System.out.println("ok=" + (total <= 64 ? 1 : 0));
        System.out.println("expected=1");
        System.out.println("ms=" + ms);
    }
}
