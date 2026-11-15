-- ============================================================================
-- 模块 8 · 专题 10：高频分组排名与窗口函数 SQL 20 题（SQLite 3.25+ / MySQL 8.0+ 通用）
-- 运行验证：sqlite3 < src/lab/sqlrank/queries.sql
-- ============================================================================

DROP TABLE IF EXISTS emp;
DROP TABLE IF EXISTS dept;
DROP TABLE IF EXISTS login_log;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS scores;
DROP TABLE IF EXISTS tree_node;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS seat;
DROP TABLE IF EXISTS weather;
DROP TABLE IF EXISTS trips;
DROP TABLE IF EXISTS users;

CREATE TABLE dept (id INT PRIMARY KEY, name VARCHAR(50));
CREATE TABLE emp (id INT PRIMARY KEY, name VARCHAR(50), salary INT, dept_id INT);
CREATE TABLE scores (id INT PRIMARY KEY, score DECIMAL(4,2));
CREATE TABLE login_log (user_id INT, login_date DATE);
CREATE TABLE orders (id INT PRIMARY KEY, user_id INT, order_date DATE, amount INT);
CREATE TABLE tree_node (id INT PRIMARY KEY, p_id INT);
CREATE TABLE courses (student VARCHAR(20), class VARCHAR(20));
CREATE TABLE seat (id INT PRIMARY KEY, student VARCHAR(20));
CREATE TABLE weather (id INT PRIMARY KEY, record_date DATE, temperature INT);
CREATE TABLE users (users_id INT PRIMARY KEY, banned VARCHAR(5), role VARCHAR(10));
CREATE TABLE trips (id INT PRIMARY KEY, client_id INT, driver_id INT, status VARCHAR(30), request_at DATE);

INSERT INTO dept VALUES (1, 'IT'), (2, 'Sales');
INSERT INTO emp VALUES
  (1, 'Joe', 85000, 1), (2, 'Henry', 80000, 2), (3, 'Sam', 60000, 2),
  (4, 'Max', 90000, 1), (5, 'Janet', 69000, 1), (6, 'Randy', 85000, 1),
  (7, 'Will', 70000, 1);
INSERT INTO scores VALUES (1, 3.50), (2, 3.65), (3, 4.00), (4, 3.85), (5, 4.00), (6, 3.65);
INSERT INTO login_log VALUES
  (1, '2026-11-01'), (1, '2026-11-02'), (1, '2026-11-03'), (1, '2026-11-05'),
  (2, '2026-11-01'), (2, '2026-11-03'),
  (3, '2026-11-02'), (3, '2026-11-03'), (3, '2026-11-04');
INSERT INTO orders VALUES
  (1, 1, '2026-10-05', 100), (2, 1, '2026-10-18', 200), (3, 1, '2026-11-02', 150),
  (4, 2, '2026-10-10', 300), (5, 2, '2026-11-01', 120);
INSERT INTO tree_node VALUES (1, NULL), (2, 1), (3, 1), (4, 2), (5, 2);
INSERT INTO courses VALUES
  ('A', 'Math'), ('B', 'English'), ('C', 'Math'), ('D', 'Biology'),
  ('E', 'Math'), ('F', 'Computer'), ('G', 'Math'), ('H', 'Math'), ('I', 'Math');
INSERT INTO seat VALUES (1, 'Abbot'), (2, 'Doris'), (3, 'Emerson'), (4, 'Green'), (5, 'Jeames');
INSERT INTO weather VALUES
  (1, '2026-11-01', 10), (2, '2026-11-02', 25), (3, '2026-11-03', 20), (4, '2026-11-04', 30);
INSERT INTO users VALUES (1, 'No', 'client'), (2, 'Yes', 'client'), (3, 'No', 'client'), (10, 'No', 'driver');
INSERT INTO trips VALUES
  (1, 1, 10, 'completed', '2026-11-01'),
  (2, 2, 10, 'cancelled_by_driver', '2026-11-01'),
  (3, 3, 10, 'cancelled_by_client', '2026-11-01'),
  (4, 1, 10, 'completed', '2026-11-02');

-- Q01: 第二高的薪水（无则返回 NULL）
SELECT 'Q01' AS q, (SELECT DISTINCT salary FROM emp ORDER BY salary DESC LIMIT 1 OFFSET 1) AS SecondHighestSalary;

-- Q02: 第 N 高的薪水（以 N=3 为例）
SELECT 'Q02' AS q, (SELECT DISTINCT salary FROM emp ORDER BY salary DESC LIMIT 1 OFFSET 2) AS NthHighestSalary;

-- Q03: 部门工资最高的员工（并列均输出）
SELECT 'Q03' AS q, d.name AS Department, t.name AS Employee, t.salary
FROM (
  SELECT *, DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS dr
  FROM emp
) t JOIN dept d ON t.dept_id = d.id
WHERE t.dr = 1;

-- Q04: 部门工资前三高的所有员工（DENSE_RANK 经典必考）
SELECT 'Q04' AS q, d.name AS Department, t.name AS Employee, t.salary
FROM (
  SELECT *, DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS dr
  FROM emp
) t JOIN dept d ON t.dept_id = d.id
WHERE t.dr <= 3;

-- Q05: 分数排名（并列不跳号 DENSE_RANK）
SELECT 'Q05' AS q, score, DENSE_RANK() OVER (ORDER BY score DESC) AS rnk FROM scores;

-- Q06: ROW_NUMBER vs RANK vs DENSE_RANK 三合一对比
SELECT 'Q06' AS q, name, dept_id, salary,
       ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rn,
       RANK()       OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rnk,
       DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS drnk
FROM emp;

-- Q07: 每组严格取 Top-2（同薪只取 id 较小者）
SELECT 'Q07' AS q, dept_id, name, salary
FROM (
  SELECT *, ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC, id ASC) AS rn
  FROM emp
) t WHERE rn <= 2;

-- Q08: 连续出现的分数（LAG 检测连续 2 次相同）
SELECT DISTINCT 'Q08' AS q, score AS ConsecutiveScore
FROM (
  SELECT score, LAG(score, 1) OVER (ORDER BY id) AS prev_s
  FROM scores
) t WHERE score = prev_s;

-- Q09: 连续登录 3 天及以上的用户（日期减行号法）
SELECT 'Q09' AS q, user_id, MIN(login_date) AS start_date, MAX(login_date) AS end_date, COUNT(*) AS streak_days
FROM (
  SELECT DISTINCT user_id, login_date,
         DATE(login_date, '-' || ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date) || ' days') AS grp
  FROM login_log
) t
GROUP BY user_id, grp
HAVING COUNT(*) >= 3;

-- Q10: 首次登录次日留存率（游戏玩法分析 IV）
WITH first_login AS (
  SELECT user_id, MIN(login_date) AS first_dt FROM login_log GROUP BY user_id
)
SELECT 'Q10' AS q,
       ROUND(1.0 * COUNT(DISTINCT l.user_id) / COUNT(DISTINCT f.user_id), 2) AS day1_retention
FROM first_login f
LEFT JOIN login_log l
  ON f.user_id = l.user_id AND l.login_date = DATE(f.first_dt, '+1 day');

-- Q11: 上升的温度（比前一天温度更高的日期）
SELECT 'Q11' AS q, id, record_date
FROM (
  SELECT id, record_date, temperature,
         LAG(temperature) OVER (ORDER BY record_date) AS prev_temp,
         LAG(record_date) OVER (ORDER BY record_date) AS prev_date
  FROM weather
) t
WHERE temperature > prev_temp AND record_date = DATE(prev_date, '+1 day');

-- Q12: 每个用户按月累计消费金额（窗口 SUM OVER）
SELECT 'Q12' AS q, user_id, order_date, amount,
       SUM(amount) OVER (PARTITION BY user_id ORDER BY order_date, id) AS running_total
FROM orders;

-- Q13: 超过 5 名学生的课（GROUP BY + HAVING COUNT(DISTINCT)）
SELECT 'Q13' AS q, class FROM courses GROUP BY class HAVING COUNT(DISTINCT student) >= 5;

-- Q14: 换座位（相邻奇偶互换）
SELECT 'Q14' AS q,
       CASE WHEN id % 2 = 1 AND id < (SELECT MAX(id) FROM seat) THEN id + 1
            WHEN id % 2 = 0 THEN id - 1
            ELSE id END AS new_id,
       student
FROM seat ORDER BY new_id;

-- Q15: 树节点分类（Root / Inner / Leaf）
SELECT 'Q15' AS q, id,
       CASE WHEN p_id IS NULL THEN 'Root'
            WHEN id IN (SELECT DISTINCT p_id FROM tree_node WHERE p_id IS NOT NULL) THEN 'Inner'
            ELSE 'Leaf' END AS node_type
FROM tree_node;

-- Q16: 行程取消率（过滤非封禁用户）
SELECT 'Q16' AS q, t.request_at AS Day,
       ROUND(1.0 * SUM(CASE WHEN t.status != 'completed' THEN 1 ELSE 0 END) / COUNT(*), 2) AS CancelRate
FROM trips t
JOIN users u ON t.client_id = u.users_id AND u.banned = 'No'
GROUP BY t.request_at;

-- Q17: 各部门薪资中位数（ROW_NUMBER + COUNT 窗口）
SELECT 'Q17' AS q, dept_id, AVG(salary) AS median_salary
FROM (
  SELECT dept_id, salary,
         ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary, id) AS rn,
         COUNT(*) OVER (PARTITION BY dept_id) AS cnt
  FROM emp
) t
WHERE rn IN ((cnt + 1) / 2, (cnt + 2) / 2)
GROUP BY dept_id;

-- Q18: 薪资高于本部门平均薪资的员工（窗口 AVG）
SELECT 'Q18' AS q, name, dept_id, salary
FROM (
  SELECT name, dept_id, salary, AVG(salary) OVER (PARTITION BY dept_id) AS dept_avg
  FROM emp
) t WHERE salary > dept_avg;

-- Q19: 删除重复记录只保留最小 id（查询保留名单）
SELECT 'Q19' AS q, MIN(id) AS keep_id, score FROM scores GROUP BY score;

-- Q20: 订单月环比增长率（LAG 窗口）
WITH monthly AS (
  SELECT STRFTIME('%Y-%m', order_date) AS ym, SUM(amount) AS rev
  FROM orders GROUP BY STRFTIME('%Y-%m', order_date)
)
SELECT 'Q20' AS q, ym, rev,
       LAG(rev) OVER (ORDER BY ym) AS prev_rev,
       ROUND(100.0 * (rev - LAG(rev) OVER (ORDER BY ym)) / LAG(rev) OVER (ORDER BY ym), 2) AS mom_pct
FROM monthly;
