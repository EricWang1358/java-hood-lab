# 模块 8：笔试算法 + SQL 手撕（10 组专题 · 101 道高频题清单）

> 定位：对标国内**初中级 Java 后端开发**笔试与一二面手撕线，按「实验先看现场 + 每天 2～3 题手敲」推进，建议总投入 **65～80 小时**。
>
> 每组专题均配有可编译运行的 Java 实验课（`src/lab/<pkg>/` + `pages/<id>.html`），先用实测数据和 `javap` 字节码搞懂「为什么暴力会超时 / 为什么边界总写错 / 为什么并发会撞坏」，再按下方清单刷题巩固。
>
> **18 道核心模板一键自测**：直接编译运行 [`M8PracticeTest.java`](../src/lab/m8practice/M8PracticeTest.java)：
> ```bash
> javac -encoding UTF-8 -d out src/lab/m8practice/M8PracticeTest.java && java -ea -cp out lab.m8practice.M8PracticeTest
> ```

---

## 专题 01（#33）：哈希与双指针（9 题）

> 实验课：[hash-twopointer.html](../pages/hash-twopointer.html) · 代码：`lab.hashtwo`

| # | 题目 | 核心模型 | 目标复杂度 |
|---|---|---|---|
| 1 | 两数之和 | `HashMap` 一次遍历存补数 | $O(n)$ / $O(n)$ |
| 2 | 字母异位词分组 | 频次数组/排序字符串作 Key | $O(nk)$ |
| 3 | 最长连续序列 | `HashSet` 只从起点 `num - 1` 不存在时扩展 | $O(n)$ |
| 4 | 移动零 | 同向快慢双指针原地交换 | $O(n)$ / $O(1)$ |
| 5 | 盛最多水的容器 | 相向双指针每次移动较短板 | $O(n)$ / $O(1)$ |
| 6 | 三数之和 | 排序 + 固定一维 + 相向双指针去重 | $O(n^2)$ |
| 7 | 无重复字符的最长子串 | 滑动窗口 + 字符上次出现下标 | $O(n)$ |
| 8 | 找到字符串中所有字母异位词 | 定长滑动窗口 + 26 维频次差 | $O(n)$ |
| 9 | 和为 K 的子数组 | 前缀和 + `HashMap` 计数 | $O(n)$ / $O(n)$ |

---

## 专题 02（#34）：链表翻转与环检测（9 题）

> 实验课：[linked-list.html](../pages/linked-list.html) · 代码：`lab.linkedlist`

| # | 题目 | 核心模型 | 目标复杂度 |
|---|---|---|---|
| 10 | 反转链表 | `prev / curr / next` 三指针迭代 | $O(n)$ / $O(1)$ |
| 11 | 反转链表 II（区间反转） | `dummy` 哨兵 + 头插法原地翻转 | $O(n)$ / $O(1)$ |
| 12 | K 个一组翻转链表 | 分组探测满 K 个再局部翻转接回 | $O(n)$ / $O(1)$ |
| 13 | 环形链表 | Floyd 快慢指针（步长 2 vs 1） | $O(n)$ / $O(1)$ |
| 14 | 环形链表 II（入环点） | 相遇后一指针回 `head` 同速再走 | $O(n)$ / $O(1)$ |
| 15 | 合并两个有序链表 | `dummy` 哨兵逐节点穿针引线 | $O(n+m)$ / $O(1)$ |
| 16 | 删除链表的倒数第 N 个结点 | `dummy` + 快指针先走 `N+1` 步 | $O(n)$ / $O(1)$ |
| 17 | 相交链表 | 双指针走完自己再走对方（消除长度差） | $O(n+m)$ / $O(1)$ |
| 18 | 排序链表 | 快慢找中点 + 归并排序 | $O(n \log n)$ |

---

## 专题 03（#35）：栈与单调队列（9 题）

> 实验课：[stack-queue.html](../pages/stack-queue.html) · 代码：`lab.stackqueue`

| # | 题目 | 核心模型 | 目标复杂度 |
|---|---|---|---|
| 19 | 有效的括号 | `ArrayDeque` 栈匹配右括号 | $O(n)$ |
| 20 | 最小栈 | 辅助单调栈同步压入当前最小值 | $O(1)$ |
| 21 | 用栈实现队列 | 双栈（输入栈 + 输出栈均摊倒腾） | 均摊 $O(1)$ |
| 22 | 每日温度 | 单调递减栈存下标找右侧首个更大值 | $O(n)$ |
| 23 | 柱状图中最大的矩形 | 单调递增栈 + 左右哨兵 `0` | $O(n)$ |
| 24 | 滑动窗口最大值 | `ArrayDeque` 单调递减队列存下标 | $O(n)$ |
| 25 | 逆波兰表达式求值 | 操作数入栈，遇运算符弹两个 | $O(n)$ |
| 26 | 基本计算器 II | 乘除先算入栈、加减带符号入栈最后求和 | $O(n)$ |
| 27 | 接雨水（单调栈版） | 递减栈遇凹槽按层横向累加 | $O(n)$ |

---

## 专题 04（#36）：二叉树递归与层序（9 题）

> 实验课：[binary-tree.html](../pages/binary-tree.html) · 代码：`lab.bintree`

| # | 题目 | 核心模型 | 目标复杂度 |
|---|---|---|---|
| 28 | 二叉树的前/中/后序遍历 | 递归 vs `ArrayDeque` 显式栈迭代 | $O(n)$ |
| 29 | 二叉树的层序遍历 | `Queue` 每层锁死 `size = queue.size()` | $O(n)$ |
| 30 | 二叉树的最大深度 | 后序分治 `1 + max(left, right)` | $O(n)$ |
| 31 | 翻转二叉树 | 前序/后序交换左右子树指针 | $O(n)$ |
| 32 | 对称二叉树 | 双指针同步递归比较外侧与内侧 | $O(n)$ |
| 33 | 二叉树的最近公共祖先 | 后序回溯：左右均非空则当前为 LCA | $O(n)$ |
| 34 | 验证二叉搜索树 | 中序遍历严格递增 / 上下界递归 | $O(n)$ |
| 35 | 从前序与中序构造二叉树 | 前序定根 + `HashMap` 查中序下标分治 | $O(n)$ |
| 36 | 二叉树中的最大路径和 | 贡献值向上返回，拐点处更新全局最大 | $O(n)$ |

---

## 专题 05（#37）：二分搜索与边界（9 题）

> 实验课：[binary-search.html](../pages/binary-search.html) · 代码：`lab.binsearch`

| # | 题目 | 核心模型 | 目标复杂度 |
|---|---|---|---|
| 37 | 二分查找 | 闭区间 `[l, r]` + `l + ((r - l) >>> 1)` | $O(\log n)$ |
| 38 | 搜索插入位置 | 找第一个 `>= target` 的左边界 | $O(\log n)$ |
| 39 | 排序数组查找首尾位置 | 两次二分分别找左边界与右边界 | $O(\log n)$ |
| 40 | 搜索旋转排序数组 | 判断哪半边有序，再决定向哪边收缩 | $O(\log n)$ |
| 41 | 寻找旋转排序数组中的最小值 | `nums[mid]` 与 `nums[r]` 比较收缩 | $O(\log n)$ |
| 42 | x 的平方根 | 整数二分防 `mid * mid` 溢出（转 `long`） | $O(\log x)$ |
| 43 | 寻找峰值 | 比较 `nums[mid]` 与 `nums[mid + 1]` 爬坡 | $O(\log n)$ |
| 44 | 爱吃香蕉的珂珂 | 答案二分：单调性判定函数 `canFinish(k)` | $O(n \log M)$ |
| 45 | 分割数组的最大值 | 答案二分：最小化子数组和的最大值 | $O(n \log S)$ |

---

## 专题 06（#38）：回溯排列与组合（9 题）

> 实验课：[backtrack.html](../pages/backtrack.html) · 代码：`lab.backtrack`

| # | 题目 | 核心模型 | 目标复杂度 |
|---|---|---|---|
| 46 | 全排列 | `used[]` 标记已选，原地 `add/remove` | $O(n \cdot n!)$ |
| 47 | 全排列 II（含重复） | 排序 + `i > 0 && nums[i] == nums[i-1] && !used[i-1]` | $O(n \cdot n!)$ |
| 48 | 子集 | 每个节点都收集，循环从 `start` 开始 | $O(n \cdot 2^n)$ |
| 49 | 子集 II（含重复） | 排序 + 同层去重 `i > start && nums[i] == nums[i-1]` | $O(n \cdot 2^n)$ |
| 50 | 组合总和（可复选） | 排序剪枝 `target - nums[i] < 0 break`，递归仍传 `i` | 指数级剪枝 |
| 51 | 组合总和 II（不可复选） | 排序 + 同层去重 + 递归传 `i + 1` | 指数级剪枝 |
| 52 | 括号生成 | 剩余左括号 `<=` 剩余右括号才可放右括号 | 卡特兰数 |
| 53 | 电话号码的字母组合 | 数字映射表 + `StringBuilder` 现场撤销 | $O(4^n)$ |
| 54 | N 皇后 | 列、主对角线、副对角线三组布尔/位掩码 | $O(n!)$ |

---

## 专题 07（#39）：基础动态规划 DP（9 题）

> 实验课：[dp-basics.html](../pages/dp-basics.html) · 代码：`lab.dpbasics`

| # | 题目 | 核心模型 | 目标复杂度 |
|---|---|---|---|
| 55 | 爬楼梯 | `dp[i] = dp[i-1] + dp[i-2]` 滚动压到 $O(1)$ | $O(n)$ / $O(1)$ |
| 56 | 打家劫舍 | `dp[i] = max(dp[i-1], dp[i-2] + nums[i])` | $O(n)$ / $O(1)$ |
| 57 | 最大子数组和 | Kadane：`cur = max(nums[i], cur + nums[i])` | $O(n)$ / $O(1)$ |
| 58 | 零钱兑换 | 完全背包求最少硬币数（内层正序） | $O(n \cdot A)$ |
| 59 | 分割等和子集 | 0-1 背包布尔可达（内层逆序防重复选） | $O(n \cdot S)$ |
| 60 | 最长递增子序列（LIS） | `dp[i]` 接前面更小值 $O(n^2)$ / 贪心二分 $O(n \log n)$ | $O(n \log n)$ |
| 61 | 最长公共子序列（LCS） | 二维 `dp[i][j]` 相等 `+1`，不等取 `max` | $O(nm)$ |
| 62 | 编辑距离 | 插入/删除/替换三转移 `1 + min(...)` | $O(nm)$ |
| 63 | 买卖股票的最佳时机 | 维护历史最低价与最大利润 | $O(n)$ / $O(1)$ |

---

## 专题 08（#40）：手写线程安全 LRU（9 题，接第 7 课）

> 实验课：[threadsafe-lru.html](../pages/threadsafe-lru.html) · 代码：`lab.tslru`

| # | 题目 | 核心模型 | 目标复杂度 |
|---|---|---|---|
| 64 | LRU 缓存（单线程手撕） | `HashMap` + 双向链表（`dummyHead/dummyTail`） | $O(1)$ |
| 65 | 线程安全 LRU（并发版） | 单锁 `synchronized` vs 16 分段 `ReentrantLock` | $O(1)$ |
| 66 | LFU 缓存 | `keyMap` + `freqMap`（频次对应双向链表）+ `minFreq` | $O(1)$ |
| 67 | O(1) 时间插入、删除和获取随机元素 | `ArrayList` + `HashMap` 尾元素交换删除 | $O(1)$ |
| 68 | 设计循环队列 | 固定数组 + `(tail + 1) % cap == head` 判满 | $O(1)$ |
| 69 | 数据流的中位数 | 大顶堆（左半）+ 小顶堆（右半）平衡 | $O(\log n)$ |
| 70 | 设计跳表 | 多层随机索引链表 | 期望 $O(\log n)$ |
| 71 | 全 O(1) 的数据结构 | `HashMap` + 计数双向链表 | $O(1)$ |
| 72 | 设计 Twitter | `HashMap` + 合并 K 个有序链表（堆） | $O(K \log K)$ |

---

## 专题 09（#41）：多线程交替打印与并发手撕（9 题）

> 实验课：[alt-print.html](../pages/alt-print.html) · 代码：`lab.altprint`

| # | 题目 | 核心模型 | 目标复杂度 |
|---|---|---|---|
| 73 | 两个线程交替打印 1～100 奇偶数 | `while` 条件 + `wait/notifyAll` | $O(n)$ |
| 74 | 三个线程交替打印 ABC（N 轮） | `ReentrantLock` + 3 个 `Condition` / 3 个 `Semaphore` | $O(n)$ |
| 75 | 交替打印 FooBar | 2 个 `Semaphore(1, 0)` 链式释放 | $O(n)$ |
| 76 | 按序打印（first/second/third） | `CountDownLatch` / `Semaphore(0)` 屏障 | $O(1)$ |
| 77 | 打印零与奇偶数 | 3 个 `Semaphore` 门控状态流转 | $O(n)$ |
| 78 | H2O 生成 | `Semaphore(2)` + `Semaphore(1)` + `CyclicBarrier(3)` | $O(n)$ |
| 79 | 手写阻塞队列（生产者消费者） | `ReentrantLock` + `notFull` / `notEmpty` 双 `Condition` | $O(1)$ |
| 80 | 哲学家进餐 | 编号奇偶反向拿叉 / 最多放 `N-1` 人防死锁 | 无死锁 |
| 81 | 手写线程安全单例（DCL） | `private static volatile` + 双重判空锁 | $O(1)$ |

---

## 专题 10（#42）：高频分组排名与窗口函数 SQL（20 题）

> 实验课：[sql-ranking.html](../pages/sql-ranking.html) · 代码：`lab.sqlrank` · 可执行脚本：[queries.sql](../src/lab/sqlrank/queries.sql)

| # | 题目 | 核心 SQL 写法 |
|---|---|---|
| 82 | 第二高的薪水 | `SELECT (SELECT DISTINCT salary ... LIMIT 1 OFFSET 1)` |
| 83 | 第 N 高的薪水 | `LIMIT 1 OFFSET N-1` |
| 84 | 部门工资最高的员工 | `DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) = 1` |
| 85 | 部门工资前三高的所有员工 | `DENSE_RANK() ... <= 3`（并列不跳号） |
| 86 | 分数排名 | `DENSE_RANK() OVER (ORDER BY score DESC)` |
| 87 | `ROW_NUMBER` vs `RANK` vs `DENSE_RANK` 对比 | 同组并列时 `1,2,3` vs `1,1,3` vs `1,1,2` |
| 88 | 每组严格取 Top-2 | `ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC, id ASC) <= 2` |
| 89 | 连续出现的数字/分数 | `LAG(score, 1) OVER (ORDER BY id)` |
| 90 | 连续登录 N 天及以上的用户 | `DATE(login_date, '-' || ROW_NUMBER() ... || ' days')` 分组计数 |
| 91 | 首次登录次日留存率 | `MIN(login_date)` + `LEFT JOIN` 次日日期 |
| 92 | 上升的温度 | `LAG(temperature)` + 确认日期差恰好 1 天 |
| 93 | 每个用户累计消费金额 | `SUM(amount) OVER (PARTITION BY user_id ORDER BY order_date, id)` |
| 94 | 超过 5 名学生的课 | `GROUP BY class HAVING COUNT(DISTINCT student) >= 5` |
| 95 | 换座位（相邻奇偶互换） | `CASE WHEN id % 2 = 1 AND id < max_id THEN id + 1 ...` |
| 96 | 树节点分类（Root / Inner / Leaf） | `p_id IS NULL` / `id IN (SELECT p_id ...)` |
| 97 | 行程取消率 | `JOIN users` 过滤 `banned = 'No'` 后按日聚合 |
| 98 | 各部门薪资中位数 | `ROW_NUMBER` + `COUNT(*) OVER` 取 `(cnt+1)/2, (cnt+2)/2` |
| 99 | 薪资高于本部门平均薪资的员工 | `AVG(salary) OVER (PARTITION BY dept_id)` |
| 100 | 删除重复记录只保留最小 id | `GROUP BY score` 取 `MIN(id)` |
| 101 | 订单月环比增长率 | `WITH monthly` + `LAG(rev) OVER (ORDER BY ym)` |
