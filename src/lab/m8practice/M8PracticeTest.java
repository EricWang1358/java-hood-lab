package lab.m8practice;

import java.util.*;
import java.util.concurrent.*;

/**
 * 模块 8 核心手撕模板自测套件（涵盖 9 大算法与并发专题的 20 道高频母题）。
 * 运行：
 *   javac -encoding UTF-8 -d out src/lab/m8practice/M8PracticeTest.java
 *   java -ea -cp out lab.m8practice.M8PracticeTest
 */
public class M8PracticeTest {

    // 1. 两数之和（HashMap 一次遍历）
    static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>((int) (nums.length / 0.75f) + 1);
        for (int i = 0; i < nums.length; i++) {
            int need = target - nums[i];
            if (map.containsKey(need)) return new int[]{map.get(need), i};
            map.put(nums[i], i);
        }
        return new int[0];
    }

    // 2. 三数之和（排序 + 双指针去重）
    static List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) {
            if (nums[i] > 0) break;
            if (i > 0 && nums[i] == nums[i - 1]) continue;
            int l = i + 1, r = nums.length - 1;
            while (l < r) {
                int s = nums[i] + nums[l] + nums[r];
                if (s == 0) {
                    res.add(Arrays.asList(nums[i], nums[l], nums[r]));
                    while (l < r && nums[l] == nums[l + 1]) l++;
                    while (l < r && nums[r] == nums[r - 1]) r--;
                    l++;
                    r--;
                } else if (s < 0) l++;
                else r--;
            }
        }
        return res;
    }

    // 3. 无重复字符的最长子串（滑动窗口）
    static int lengthOfLongestSubstring(String s) {
        int[] last = new int[128];
        Arrays.fill(last, -1);
        int left = 0, ans = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (last[c] >= left) left = last[c] + 1;
            last[c] = right;
            ans = Math.max(ans, right - left + 1);
        }
        return ans;
    }

    // 4. 和为 K 的子数组（前缀和 + HashMap）
    static int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> preCount = new HashMap<>();
        preCount.put(0, 1);
        int sum = 0, ans = 0;
        for (int x : nums) {
            sum += x;
            ans += preCount.getOrDefault(sum - k, 0);
            preCount.merge(sum, 1, Integer::sum);
        }
        return ans;
    }

    static class ListNode {
        int val;
        ListNode next;
        ListNode(int val) { this.val = val; }
    }

    // 5. 反转链表（三指针迭代）
    static ListNode reverseList(ListNode head) {
        ListNode prev = null, curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }

    // 6. 环形链表 II（找入环点）
    static ListNode detectCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) {
                ListNode p = head;
                while (p != slow) {
                    p = p.next;
                    slow = slow.next;
                }
                return p;
            }
        }
        return null;
    }

    // 7. 有效括号（ArrayDeque 栈）
    static boolean isValid(String s) {
        Deque<Character> st = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(') st.push(')');
            else if (c == '[') st.push(']');
            else if (c == '{') st.push('}');
            else if (st.isEmpty() || st.pop() != c) return false;
        }
        return st.isEmpty();
    }

    // 8. 滑动窗口最大值（单调递减队列）
    static int[] maxSlidingWindow(int[] nums, int k) {
        int n = nums.length;
        int[] res = new int[n - k + 1];
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            while (!q.isEmpty() && nums[q.peekLast()] <= nums[i]) q.pollLast();
            q.offerLast(i);
            if (q.peekFirst() <= i - k) q.pollFirst();
            if (i >= k - 1) res[i - k + 1] = nums[q.peekFirst()];
        }
        return res;
    }

    static class TreeNode {
        int val;
        TreeNode left, right;
        TreeNode(int val) { this.val = val; }
    }

    // 9. 二叉树层序遍历（锁死 size）
    static List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> res = new ArrayList<>();
        if (root == null) return res;
        Queue<TreeNode> q = new ArrayDeque<>();
        q.offer(root);
        while (!q.isEmpty()) {
            int size = q.size();
            List<Integer> level = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                TreeNode cur = q.poll();
                level.add(cur.val);
                if (cur.left != null) q.offer(cur.left);
                if (cur.right != null) q.offer(cur.right);
            }
            res.add(level);
        }
        return res;
    }

    // 10. 二叉树最近公共祖先（后序分治）
    static TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        if (root == null || root == p || root == q) return root;
        TreeNode left = lowestCommonAncestor(root.left, p, q);
        TreeNode right = lowestCommonAncestor(root.right, p, q);
        if (left != null && right != null) return root;
        return left != null ? left : right;
    }

    // 11. 二分查找左右边界（闭区间模板）
    static int[] searchRange(int[] nums, int target) {
        return new int[]{findBound(nums, target, true), findBound(nums, target, false)};
    }

    static int findBound(int[] nums, int target, boolean left) {
        int l = 0, r = nums.length - 1, res = -1;
        while (l <= r) {
            int mid = l + ((r - l) >>> 1);
            if (nums[mid] == target) {
                res = mid;
                if (left) r = mid - 1;
                else l = mid + 1;
            } else if (nums[mid] < target) l = mid + 1;
            else r = mid - 1;
        }
        return res;
    }

    // 12. 含重复元素子集（排序 + 同层去重）
    static List<List<Integer>> subsetsWithDup(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();
        backtrackSubsets(nums, 0, new ArrayList<>(), res);
        return res;
    }

    static void backtrackSubsets(int[] nums, int start, List<Integer> path, List<List<Integer>> res) {
        res.add(new ArrayList<>(path));
        for (int i = start; i < nums.length; i++) {
            if (i > start && nums[i] == nums[i - 1]) continue;
            path.add(nums[i]);
            backtrackSubsets(nums, i + 1, path, res);
            path.remove(path.size() - 1);
        }
    }

    // 13. 含重复元素全排列（排序 + used[] 去重）
    static List<List<Integer>> permuteUnique(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> res = new ArrayList<>();
        backtrackPermute(nums, new boolean[nums.length], new ArrayList<>(), res);
        return res;
    }

    static void backtrackPermute(int[] nums, boolean[] used, List<Integer> path, List<List<Integer>> res) {
        if (path.size() == nums.length) {
            res.add(new ArrayList<>(path));
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            if (i > 0 && nums[i] == nums[i - 1] && !used[i - 1]) continue;
            used[i] = true;
            path.add(nums[i]);
            backtrackPermute(nums, used, path, res);
            path.remove(path.size() - 1);
            used[i] = false;
        }
    }

    // 14. 零钱兑换（完全背包，内层正序）
    static int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);
        dp[0] = 0;
        for (int c : coins) {
            for (int j = c; j <= amount; j++) {
                dp[j] = Math.min(dp[j], dp[j - c] + 1);
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }

    // 15. 分割等和子集（0-1 背包，内层逆序）
    static boolean canPartition(int[] nums) {
        int sum = 0;
        for (int x : nums) sum += x;
        if ((sum & 1) == 1) return false;
        int target = sum / 2;
        boolean[] dp = new boolean[target + 1];
        dp[0] = true;
        for (int x : nums) {
            for (int j = target; j >= x; j--) {
                dp[j] = dp[j] || dp[j - x];
            }
        }
        return dp[target];
    }

    // 16. 最长递增子序列 LIS（贪心 + 二分 O(n log n)）
    static int lengthOfLIS(int[] nums) {
        int[] tails = new int[nums.length];
        int size = 0;
        for (int x : nums) {
            int l = 0, r = size;
            while (l < r) {
                int mid = l + ((r - l) >>> 1);
                if (tails[mid] < x) l = mid + 1;
                else r = mid;
            }
            tails[l] = x;
            if (l == size) size++;
        }
        return size;
    }

    // 17. 手写线程安全 LRU（HashMap + dummy 双向链表 + synchronized）
    static class SafeLRU {
        static class Node {
            int k, v;
            Node prev, next;
            Node(int k, int v) { this.k = k; this.v = v; }
        }
        private final int cap;
        private final Map<Integer, Node> map = new HashMap<>();
        private final Node head = new Node(0, 0), tail = new Node(0, 0);

        SafeLRU(int cap) {
            this.cap = cap;
            head.next = tail;
            tail.prev = head;
        }

        synchronized int get(int k) {
            Node n = map.get(k);
            if (n == null) return -1;
            remove(n);
            addFirst(n);
            return n.v;
        }

        synchronized void put(int k, int v) {
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

    // 18. 三线程交替打印 ABC（Semaphore 链式接力）
    static String altPrintABC(int rounds) throws Exception {
        Semaphore[] sems = {new Semaphore(1), new Semaphore(0), new Semaphore(0)};
        StringBuilder sb = new StringBuilder();
        Thread[] ts = new Thread[3];
        for (int id = 0; id < 3; id++) {
            final int myId = id;
            final char ch = (char) ('A' + id);
            ts[id] = new Thread(() -> {
                for (int r = 0; r < rounds; r++) {
                    try {
                        sems[myId].acquire();
                        sb.append(ch);
                        sems[(myId + 1) % 3].release();
                    } catch (InterruptedException ignored) {}
                }
            });
            ts[id].start();
        }
        for (Thread t : ts) t.join();
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        assert Arrays.equals(twoSum(new int[]{2, 7, 11, 15}, 9), new int[]{0, 1});
        assert threeSum(new int[]{-1, 0, 1, 2, -1, -4}).size() == 2;
        assert lengthOfLongestSubstring("abcabcbb") == 3;
        assert subarraySum(new int[]{1, 1, 1}, 2) == 2;

        ListNode n1 = new ListNode(1), n2 = new ListNode(2), n3 = new ListNode(3);
        n1.next = n2; n2.next = n3;
        ListNode rev = reverseList(n1);
        assert rev.val == 3 && rev.next.val == 2 && rev.next.next.val == 1;

        ListNode c1 = new ListNode(3), c2 = new ListNode(2), c3 = new ListNode(0), c4 = new ListNode(-4);
        c1.next = c2; c2.next = c3; c3.next = c4; c4.next = c2;
        assert detectCycle(c1) == c2;

        assert isValid("()[]{}") && !isValid("([)]");
        assert Arrays.equals(maxSlidingWindow(new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3), new int[]{3, 3, 5, 5, 6, 7});

        TreeNode t1 = new TreeNode(3), t2 = new TreeNode(9), t3 = new TreeNode(20), t4 = new TreeNode(15), t5 = new TreeNode(7);
        t1.left = t2; t1.right = t3; t3.left = t4; t3.right = t5;
        assert levelOrder(t1).size() == 3;
        assert lowestCommonAncestor(t1, t4, t5) == t3;

        assert Arrays.equals(searchRange(new int[]{5, 7, 7, 8, 8, 10}, 8), new int[]{3, 4});
        assert subsetsWithDup(new int[]{1, 2, 2}).size() == 6;
        assert permuteUnique(new int[]{1, 1, 2}).size() == 3;
        assert coinChange(new int[]{1, 2, 5}, 11) == 3;
        assert canPartition(new int[]{1, 5, 11, 5}) && !canPartition(new int[]{1, 2, 3, 5});
        assert lengthOfLIS(new int[]{10, 9, 2, 5, 3, 7, 101, 18}) == 4;

        SafeLRU lru = new SafeLRU(2);
        lru.put(1, 1); lru.put(2, 2);
        assert lru.get(1) == 1;
        lru.put(3, 3);
        assert lru.get(2) == -1;

        assert altPrintABC(3).equals("ABCABCABC");
        System.out.println("ALL 18 CORE MODULE-8 TEMPLATES PASSED!");
    }
}
