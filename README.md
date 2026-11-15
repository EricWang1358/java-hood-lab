# Java Hood Lab — 掀开机盖学 Java

> **不背口诀，先看现场**：基于真实压测数据与 `javap` 字节码证据的交互式 Java 实验课。
>
> *Interactive Java labs — guess first, break it for real, inspect bytecode, and choose tools by measured trade-offs.*

## 在线学习与快速开始

- **在线体验（GitHub Pages）**：[https://ericwang1358.github.io/java-hood-lab/](https://ericwang1358.github.io/java-hood-lab/)
- **本地离线学习**：克隆或下载本仓库后，直接双击打开 [`index.html`](./index.html)（或 [`pages/`](./pages/) 下任意 `.html`），**零外部依赖**即可使用。
- **操作方式**：点击「下一步」或按 `→` / `空格` 推进讲解，按 `←` 回退；点击带虚线下划线的术语可弹出大白话解释；点击右上角可切换亮/暗配色与课堂笔记。

## 每节课的设计闭环

1. **先猜后撞**：先让你基于直觉下注，再放出 20 次实测运行散点对照，亲眼看见为什么会出事。
2. **掀开机盖**：调取真实 `javap -c -p` 字节码指令与双线程交错泳道（甚至手动排班触发撞车），用证据替代断言。
3. **算清代价**：在 1/2/4/8 等不同并发挡位下对比多种修复写法的真实耗时折线，提炼选型规则并用决策树当场练题。
4. **开口带走**：2 分钟计时自述检验 + 最多 3 张核心复习卡。

## 目录结构

```text
.
├── index.html   # 80 节实验课总导航入口页（支持搜索、分章筛选与亮暗主题）
├── pages/       # 80 份单文件交互式课件网页（双击即开，零外链依赖）
├── lessons/     # 80 份课程结构与讲解数据（JSON）
├── src/         # 320 份可独立编译运行的 Java 对照源码（每课 4 种变体）+ SQL 20 题脚本
├── results/     # 实验台在本机实测沉淀的运行数据与 javap 输出记录（JSON）
└── docs/        # 模块 8 101 道高频笔试算法与 SQL 手撕刷题清单
```

## 完整课表（共 80 课）

### 第一章：竞态与并发容器（第 1～7 课）

从最基础的 count++ 丢数出发，建立「读-改-写回被插队」模型，打破「换上线程安全容器/写时复制就绝对安全」的误区。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 01 | **两个线程一起数数，能数对吗** | 每个线程把同一个数加 100 万次。两个线程一起，最后是多少？ | `lab.counter` | [网页课件](./pages/counter-race.html) · [JSON](./lessons/counter-race.json) · [源码](./src/lab/counter/) |
| 02 | **库存只有这么多，会卖超吗** | 几个线程一起卖货，每次先看有没有货，有就拿走一件。最后会卖出多少？ | `lab.stock` | [网页课件](./pages/stock-oversell.html) · [JSON](./lessons/stock-oversell.json) · [源码](./src/lab/stock/) |
| 03 | **换了安全容器，销量怎么还是少？** | 大家都说线程安全的 ConcurrentHashMap，先取再加一放回为什么照样丢？ | `lab.tally` | [网页课件](./pages/tally-map.html) · [JSON](./lessons/tally-map.json) · [源码](./src/lab/tally/) |
| 04 | **换成 putIfAbsent，连接还会多建吗？** | 懒加载缓存时换成 ConcurrentHashMap 的 putIfAbsent，为什么连接照样被新建了好几遍？ | `lab.absent` | [网页课件](./pages/absent-init.html) · [JSON](./lessons/absent-init.json) · [源码](./src/lab/absent/) |
| 05 | **列表加 volatile，日志还是少？** | 多线程把日志一条条写进共享列表，为什么不光少条数，给列表加上 volatile 照样挡不住？ | `lab.listadd` | [网页课件](./pages/list-append.html) · [JSON](./lessons/list-append.json) · [源码](./src/lab/listadd/) |
| 06 | **加了同步包装，遍历还是炸？** | 读多写少的白名单换成 Collections.synchronizedList，为什么一边遍历一边改照样炸出异常？ | `lab.cow` | [网页课件](./pages/cow-iter.html) · [JSON](./lessons/cow-iter.json) · [源码](./src/lab/cow/) |
| 07 | **只调 get 不修改，读写锁怎么也撞坏？** | 开启按访问排序的 LinkedHashMap 后，连读取都要改双向链表；以为只读就上 readLock 照样把缓存搞碎。 | `lab.lru` | [网页课件](./pages/lru-visit.html) · [JSON](./lessons/lru-visit.json) · [源码](./src/lab/lru/) |

### 第二章：锁的边界、读写分离与死锁（第 8～12 课）

为什么加了 synchronized 还是锁不住？弄清锁对象身份、装箱陷阱、读写锁与乐观读切点，以及按序加锁防死锁。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 08 | **方法都加了 synchronized，怎么还丢数？** | 给 add() 加上了 synchronized，为什么每个线程各自 new 对象来调时，静态总账还是照样少？ | `lab.lockscope` | [网页课件](./pages/lock-scope.html) · [JSON](./lessons/lock-scope.json) · [源码](./src/lab/lockscope/) |
| 09 | **锁在 static 变量上，怎么照样锁不住？** | 这次锁对象老实写成了 static 全局共享，可只因为把 Integer count 拿去加锁，它竟在眼皮底下被掉包了。 | `lab.boxlock` | [网页课件](./pages/box-lock.html) · [JSON](./lessons/box-lock.json) · [源码](./src/lab/boxlock/) |
| 10 | **读多写少，读写锁什么时候真该请出场？** | 两个关联字段都加上 volatile 照样读到半截数；真正只读且读远多于写时，读写锁怎么胜出？ | `lab.rwlock` | [网页课件](./pages/rw-lock.html) · [JSON](./lessons/rw-lock.json) · [源码](./src/lab/rwlock/) |
| 11 | **不占读锁也能防撕裂吗** | 用版本戳替掉读写锁的读计数，为什么还是读出半新半旧的一对数？ | `lab.stamped` | [网页课件](./pages/stamped-opt.html) · [JSON](./lessons/stamped-opt.json) · [源码](./src/lab/stamped/) |
| 12 | **两个账户各自上锁，转账怎么还卡死？** | 前面学会了 ReentrantLock 与读写锁，可只要一次操作同时要拿两把锁，照着参数顺序各自加锁竟会互相卡死。 | `lab.deadlock` | [网页课件](./pages/deadlock-order.html) · [JSON](./lessons/deadlock-order.json) · [源码](./src/lab/deadlock/) |

### 第三章：无锁 CAS、分段累加与不可变快照（第 13～15 课）

直面 CAS 的 ABA 欺骗、LongAdder 散列计数为何不能卡硬上限，以及多字段关联时「两个原子类拼接仍撕裂」的真相。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 13 | **值变回 19，CAS 怎么还是发重了？** | 第 2 节学过无锁 CAS，可若值先从 A 变 B 又改回 A，只查当前值的 compareAndSet 会怎样？ | `lab.aba` | [网页课件](./pages/cas-aba.html) · [JSON](./lessons/cas-aba.json) · [源码](./src/lab/aba/) |
| 14 | **用 LongAdder 抢券为什么还是超发？** | 面经说 LongAdder 更快，可用来卡抢券上限怎么照样超发？ | `lab.adder` | [网页课件](./pages/adder-limit.html) · [JSON](./lessons/adder-limit.json) · [源码](./src/lab/adder/) |
| 15 | **两字段各自换成原子类，还读半套？** | 折扣和门槛必须配套生效；各换成 AtomicInteger 分开改，怎么还是读出新折扣配旧门槛？ | `lab.multiref` | [网页课件](./pages/multi-ref.html) · [JSON](./lessons/multi-ref.json) · [源码](./src/lab/multiref/) |

### 第四章：线程封闭、线程池反压与异步编排（第 16～21 课）

告别共享与排队：彻底厘清无状态局部隔离、ThreadLocal 池复用串号、异步快照透传、队列反压策略与异步汇总失误。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 16 | **静态日期工具为啥会串味？** | 看似无状态的静态日期格式化，并发下一方面串出别人日期，另一方面直接抛异常。 | `lab.datefmt` | [网页课件](./pages/date-format.html) · [JSON](./lessons/date-format.json) · [源码](./src/lab/datefmt/) |
| 17 | **单例成员暂存单价，为何一并发就串单？** | 看似先写后用没毛病，一多线程全乱套，加 volatile 也白搭。 | `lab.singleton` | [网页课件](./pages/singleton-state.html) · [JSON](./lessons/singleton-state.json) · [源码](./src/lab/singleton/) |
| 18 | **线程各有口袋，进线程池怎么还串号？** | 每个线程独享一份上下文，进了线程池反复复用，为什么还是把上一单身份串给了下一单？ | `lab.tlpool` | [网页课件](./pages/threadlocal-pool.html) · [JSON](./lessons/threadlocal-pool.json) · [源码](./src/lab/tlpool/) |
| 19 | **异步丢进线程池，traceId 还能传过去吗？** | 上游把 traceId 放进 ThreadLocal，进线程池却读成 null；换可继承版怎么还串号？ | `lab.tlinherit` | [网页课件](./pages/tl-inherit.html) · [JSON](./lessons/tl-inherit.json) · [源码](./src/lab/tlinherit/) |
| 20 | **队列满了换「不报错」策略，怎么还在丢单？** | 吞掉拒单异常或换成不抛错的丢弃策略，表面一片安静，订单却悄悄没了；怎么在不撑爆队列的前提下反压保单？ | `lab.poolreject` | [网页课件](./pages/pool-reject.html) · [JSON](./lessons/pool-reject.json) · [源码](./src/lab/poolreject/) |
| 21 | **allOf 等完了，总账怎么还是错？** | 三个异步分支并行算优惠、积分和运费，加了 allOf().join() 等收尾，为何共享总账照样丢更新？ | `lab.cfcombine` | [网页课件](./pages/cf-combine.html) · [JSON](./lessons/cf-combine.json) · [源码](./src/lab/cfcombine/) |

### 第五章：高并发工程模式与新特性实战（第 22～32 课）

把单机并发推向真实后端生产场景：Semaphore 限流、按用户加锁、CAS 状态机、SingleFlight 防击穿、分段扣库存、位打包发号、DCL 双检锁、虚拟线程与 Outbox 发件箱。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 22 | **Semaphore：同时只放 2 个线程** | 下游最多扛 2 并发，先查再加照样打超，互斥锁又只放 1 个，用 Semaphore 精准卡在 2。 | `lab.semgate` | [网页课件](./pages/semaphore-gate.html) · [JSON](./lessons/semaphore-gate.json) · [源码](./src/lab/semgate/) |
| 23 | **每人限领一次：按用户加锁** | 同一个人连点两下要拦住，不相干的人还得同时领。 | `lab.userlock` | [网页课件](./pages/once-per-user.html) · [JSON](./lessons/once-per-user.json) · [源码](./src/lab/userlock/) |
| 24 | **同一张订单，怎么又付款又取消？** | 支付回调和超时关单同时扑向一张待支付订单，为什么连 volatile 也拦不住双方同时放行？ | `lab.orderstate` | [网页课件](./pages/order-state.html) · [JSON](./lessons/order-state.json) · [源码](./src/lab/orderstate/) |
| 25 | **换成安全表先查后放，怎么还会多查库？** | 缓存已换安全表，先判空查库再调 putIfAbsent；冷键并发一来，数据库还是被重复查了很多遍。 | `lab.cacheaside` | [网页课件](./pages/cache-aside.html) · [JSON](./lessons/cache-aside.json) · [源码](./src/lab/cacheaside/) |
| 26 | **热点库存分片：16 桶为何还超卖** | 爆品秒杀挤死一把全局锁，有人把库存拆进 16 个普通桶却照样卖超；改用分段 CAS 加顺延探测才稳。 | `lab.shardstock` | [网页课件](./pages/sharded-stock.html) · [JSON](./lessons/sharded-stock.json) · [源码](./src/lab/shardstock/) |
| 27 | **位打包 CAS 发号：告别跨窗重号** | 把时间窗和序列号分开放，为什么加了原子类跨窗瞬间还是会撞出重复 ID？ | `lab.idgen` | [网页课件](./pages/id-gen.html) · [JSON](./lessons/id-gen.json) · [源码](./src/lab/idgen/) |
| 28 | **两数各换 AtomicLong，均值还算劈？** | 实时延迟监控累加 (count, sum)，每笔 10ms；各自换成原子类，为什么还会算出错位均值？ | `lab.compavg` | [网页课件](./pages/compound-avg.html) · [JSON](./lessons/compound-avg.json) · [源码](./src/lab/compavg/) |
| 29 | **外面判空里面又加锁，怎么还建出两份实例？** | 只在锁外判空、进锁直接建，并发仍造两份；全方法加锁拖慢纯读，靠 volatile 双检锁只建一次又秒读。 | `lab.lazysingle` | [网页课件](./pages/lazy-singleton.html) · [JSON](./lessons/lazy-singleton.json) · [源码](./src/lab/lazysingle/) |
| 30 | **换了安全队列再 clear，怎么还丢日志？** | 并发安全的队列保得住单次入队，却护不住「遍历拷贝」与「整队清空」之间的间隙。 | `lab.queuedrain` | [网页课件](./pages/queue-drain.html) · [JSON](./lessons/queue-drain.json) · [源码](./src/lab/queuedrain/) |
| 31 | **换上虚拟线程，共享计数还需要锁吗** | 只把平台线程换成 Thread.ofVirtual()，百万次 count++ 会少掉吗？ | `lab.virtrace` | [网页课件](./pages/virtual-race.html) · [JSON](./lessons/virtual-race.json) · [源码](./src/lab/virtrace/) |
| 32 | **库与缓存各用原子数组，为啥还会写反？** | 更新主库并同步写投影，两边都换原子数组后还是被慢请求倒盖；全系列收官，怎么彻底对齐？ | `lab.txoutbox` | [网页课件](./pages/tx-outbox.html) · [JSON](./lessons/tx-outbox.json) · [源码](./src/lab/txoutbox/) |

### 模块三 · 第一章：TCP 传输与 Socket 底层（M3-01～M3-05）

用真实 Socket 实测三次握手建连成本、`TIME_WAIT` 端口复用、全连接队列 `backlog` 拥堵、TCP 粘包拆包与 Nagle 延迟 ACK 撞车。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M3-01 | **短连接每次都要三次握手，有多慢？** | 同样发 200 次小请求，每次新建 Socket 挥手断开 vs 复用长连接，耗时差多少？ | `lab.tcphandshake` | [网页课件](./pages/tcp-handshake.html) · [JSON](./lessons/tcp-handshake.json) · [源码](./src/lab/tcphandshake/) |
| M3-02 | **压测刚跑几千次就 BindException？** | 服务端或客户端频繁主动断开连接时，为什么端口很快被 `TIME_WAIT` 占满报错？ | `lab.timewait` | [网页课件](./pages/tcp-timewait.html) · [JSON](./lessons/tcp-timewait.json) · [源码](./src/lab/timewait/) |
| M3-03 | **accept 慢了一拍，新连接怎么直接被拒？** | 服务端 `ServerSocket` 来不及 `accept()` 时，内核全连接队列 `backlog` 装满会发生什么？ | `lab.backlog` | [网页课件](./pages/tcp-backlog.html) · [JSON](./lessons/tcp-backlog.json) · [源码](./src/lab/backlog/) |
| M3-04 | **连发 100 条消息，怎么粘成一坨还缺半截？** | TCP 是无边界的字节流，连发 100 条 `"MSG-i"`，为什么接收方按 `read(buf)` 读出来个数不对？ | `lab.sticky` | [网页课件](./pages/tcp-sticky.html) · [JSON](./lessons/tcp-sticky.json) · [源码](./src/lab/sticky/) |
| M3-05 | **小包发得越勤反而越慢？** | 连续发送小数据包并等待确认时，为什么 Nagle 算法撞上延迟 ACK 会让耗时暴涨？ | `lab.nagle` | [网页课件](./pages/tcp-nagle.html) · [JSON](./lessons/tcp-nagle.json) · [源码](./src/lab/nagle/) |

### 模块四 · 第一章：SQL 必知必会、联表与深分页改写（第 33～37 课）

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 33 | **循环查库 N+1，接口怎么慢成蜗牛？** | 先查出 1000 条订单，再在 for 循环里一条条查下单人名字。每次只查 1 行，接口为什么还是慢到超时？ | `lab.sqljoin` | [网页课件](./pages/sql-join-nplus1.html) · [JSON](./lessons/sql-join-nplus1.json) · [源码](./src/lab/sqljoin/) |
| 34 | **翻到第 10 万页，LIMIT 为啥突然卡死？** | 同样每页只取 20 条，LIMIT 0, 20 只要 1 毫秒，LIMIT 100000, 20 为什么突然变成慢查询？ | `lab.deeppage` | [网页课件](./pages/sql-deep-paging.html) · [JSON](./lessons/sql-deep-paging.json) · [源码](./src/lab/deeppage/) |
| 35 | **COUNT(*) 到底比 COUNT(1) 慢多少？** | 面试常问 COUNT(*)、COUNT(1)、COUNT(列名) 谁最快。用错不仅慢，COUNT(列名) 还会把统计数算少！ | `lab.countstar` | [网页课件](./pages/sql-count-star.html) · [JSON](./lessons/sql-count-star.json) · [源码](./src/lab/countstar/) |
| 36 | **WHERE 和 HAVING 混用，全表都被扫了一遍？** | 同样是筛选 status = 'PAID'，写在 WHERE 里只要 2 毫秒，写在 HAVING 里为什么把临时表撑爆？ | `lab.wherehaving` | [网页课件](./pages/sql-group-having.html) · [JSON](./lessons/sql-group-having.json) · [源码](./src/lab/wherehaving/) |
| 37 | **字段允许 NULL，!= 怎么把数据全漏掉了？** | 用 WHERE id NOT IN (SELECT ...) 查黑名单外用户，子查询里只混进了一个 NULL，整条 SQL 竟然返回 0 行！ | `lab.nulltrap` | [网页课件](./pages/sql-null-trap.html) · [JSON](./lessons/sql-null-trap.json) · [源码](./src/lab/nulltrap/) |

### 模块四 · 第二章：InnoDB 页结构、B+ 树聚簇索引、回表与最左前缀（第 43～49 课）

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 43 | **UUID 当主键，插入怎么越插越慢？** | 用随机 UUID 当 InnoDB 主键，数据一上百万插入就抖动变慢；换成自增 ID 或有序 UUIDv7 为什么立刻稳了？ | `lab.pagesplit` | [网页课件](./pages/innodb-page-split.html) · [JSON](./lessons/innodb-page-split.json) · [源码](./src/lab/pagesplit/) |
| 44 | **为什么 MySQL 索引选 B+ 树不选哈希或二叉树？** | 哈希查找明明是 O(1)，为什么 InnoDB 索引偏偏要用 B+ 树？做一次 BETWEEN 范围查询就全明白了。 | `lab.btreeio` | [网页课件](./pages/btree-vs-hash.html) · [JSON](./lessons/btree-vs-hash.json) · [源码](./src/lab/btreeio/) |
| 45 | **走索引了，为什么还要回表再查一遍？** | 明明给 name 建了索引，EXPLAIN 也显示走了 idx_name，怎么查 2000 行还是要查 4000 次树？ | `lab.lookup` | [网页课件](./pages/clustered-lookup.html) · [JSON](./lessons/clustered-lookup.json) · [源码](./src/lab/lookup/) |
| 46 | **把 SELECT * 改成两列，耗时怎么直接腰斩？** | 生产规范总说「禁止 SELECT *」。把 SELECT * 改成 SELECT id, name 后，EXPLAIN 多了句 Using index，发生了什么？ | `lab.covering` | [网页课件](./pages/covering-index.html) · [JSON](./lessons/covering-index.json) · [源码](./src/lab/covering/) |
| 47 | **建了 (a, b, c) 联合索引，查 b 为啥不走索引？** | 明明建了联合索引 (a, b, c)，结果一查 WHERE b = 500 就变成了全表扫描，怎么回事？ | `lab.leftmost` | [网页课件](./pages/leftmost-prefix.html) · [JSON](./lessons/leftmost-prefix.json) · [源码](./src/lab/leftmost/) |
| 48 | **联合索引范围后面的列，ICP 怎么少回一半表？** | 建了 (name, age) 联合索引，查 name LIKE '张%' AND age = 20。age 明明被范围截断了，为什么 MySQL 5.6 之后还是变快了？ | `lab.icp` | [网页课件](./pages/index-condition-pushdown.html) · [JSON](./lessons/index-condition-pushdown.json) · [源码](./src/lab/icp/) |
| 49 | **字符串建前缀索引，ORDER BY 怎么还文件排序？** | 给长邮箱建了前缀索引 idx_email(email(8)) 省了不少空间，可不仅回表变多，ORDER BY email 竟然也用不上索引排序！ | `lab.prefixidx` | [网页课件](./pages/prefix-index-order.html) · [JSON](./lessons/prefix-index-order.json) · [源码](./src/lab/prefixidx/) |

### 模块四 · 第三章：EXPLAIN 慢 SQL 诊断与索引失效现场（第 50～54 课）

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 50 | **EXPLAIN 里 type=ALL 和 ref，扫描行数差多少？** | 慢 SQL 排查第一件事就是敲 EXPLAIN。type 列里 ALL、index、range、ref 到底差几个数量级？ | `lab.explaintype` | [网页课件](./pages/explain-type-cost.html) · [JSON](./lessons/explain-type-cost.json) · [源码](./src/lab/explaintype/) |
| 51 | **索引列套了个函数或隐式转换，索引怎么直接哑火？** | 明明给 phone 和 created_at 建了索引，结果 WHERE YEAR(created_at)=2026 和 WHERE phone=138... 全都退化成全表扫描！ | `lab.idxfunc` | [网页课件](./pages/index-func-implicit.html) · [JSON](./lessons/index-func-implicit.json) · [源码](./src/lab/idxfunc/) |
| 52 | **LIKE '%abc' 和 OR 没加索引，为啥全表扫？** | LIKE 'abc%' 能走索引，加了个前导百分号 LIKE '%abc' 就废了；WHERE a=1 OR b=2 只要 b 没索引，连 a 的索引也一起拖下水！ | `lab.likeor` | [网页课件](./pages/index-like-or.html) · [JSON](./lessons/index-like-or.json) · [源码](./src/lab/likeor/) |
| 53 | **范围查询后面的联合索引列，怎么突然失效了？** | 建了 (a, b, c) 联合索引，查 a=1 AND b>10 AND c=2。明明三列全在条件里，为什么 key_len 显示只用到了 (a, b)？ | `lab.rangebreak` | [网页课件](./pages/index-range-break.html) · [JSON](./lessons/index-range-break.json) · [源码](./src/lab/rangebreak/) |
| 54 | **ORDER BY 和 GROUP BY 混用，Extra 怎么冒出 Using filesort？** | 明明只取 LIMIT 20，为什么 EXPLAIN 里出现了 Using filesort，慢到把 Sort Buffer 打满甚至落盘归并？ | `lab.filesort` | [网页课件](./pages/filesort-temp-table.html) · [JSON](./lessons/filesort-temp-table.json) · [源码](./src/lab/filesort/) |

### 模块五 · 第一章：Spring 事务失效、动态代理与 AOP 切面（第 38～42 课）

用纯 JDK 动态代理与字节码证据，亲手复现同类自调用、异常吞没、传播行为连坐回滚、JDK/CGLIB 代理坑与多切面洋葱错序。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 38 | **同类自调用，@Transactional 为什么没回滚？** | 外层方法没加事务，内部直接 this.payAndRecord() 调了加 @Transactional 的方法。抛异常后，库存却没回滚！ | `lab.txself` | [网页课件](./pages/spring-tx-self.html) · [JSON](./lessons/spring-tx-self.json) · [源码](./src/lab/txself/) |
| 39 | **异常被吞或抛检查异常，事务怎么照样提交了？** | 明明加了 @Transactional，也确实出错了——但要么被 try-catch 吞掉，要么抛了 IOException，结果扣款照样 commit！ | `lab.txswallow` | [网页课件](./pages/spring-tx-swallow.html) · [JSON](./lessons/spring-tx-swallow.json) · [源码](./src/lab/txswallow/) |
| 40 | **外层 catch 住内层异常，整个事务怎么还是全炸了？** | 主流程加了事务，调用「加积分」子方法时用 try-catch 兜住了异常——结果提交时竟抛出 UnexpectedRollbackException，连主订单一起回滚！ | `lab.txprop` | [网页课件](./pages/spring-tx-prop.html) · [JSON](./lessons/spring-tx-prop.json) · [源码](./src/lab/txprop/) |
| 41 | **JDK 动态代理 vs CGLIB：强转就炸、final 就失效** | 有接口用 JDK 代理，强转实现类直接 ClassCastException；换成 CGLIB 子类代理，加个 final 切面就静默失效！ | `lab.proxykind` | [网页课件](./pages/spring-proxy-kind.html) · [JSON](./lessons/spring-proxy-kind.json) · [源码](./src/lab/proxykind/) |
| 42 | **多个 @Aspect 切面叠在一起：锁、日志、事务谁先谁后？** | 下单方法同时挂了分布式锁切面和 @Transactional。如果锁在事务里面——锁都放开了事务还没提交，下一个线程直接读到旧余额！ | `lab.aoporder` | [网页课件](./pages/spring-aop-order.html) · [JSON](./lessons/spring-aop-order.json) · [源码](./src/lab/aoporder/) |

### 模块五 · 第二章：IoC 容器、Bean 生命周期与自动装配（第 43～47 课）

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 43 | **三级缓存解循环依赖：为什么非要第三级 ObjectFactory？** | A 依赖 B、B 又依赖 A。如果只有两级缓存直接暴露半成品对象，遇上 AOP 代理，B 拿到的竟是未代理的裸对象！ | `lab.ioccycle` | [网页课件](./pages/spring-ioc-cycle.html) · [JSON](./lessons/spring-ioc-cycle.json) · [源码](./src/lab/ioccycle/) |
| 44 | **单例 Bean 里注入 Prototype 或成员变量，并发怎么串单了？** | Spring Bean 默认是单例。在 Service 里写个成员变量暂存 userId，或者直接注入 Prototype Bean，多线程一来全串单！ | `lab.beanscope` | [网页课件](./pages/spring-bean-scope.html) · [JSON](./lessons/spring-bean-scope.json) · [源码](./src/lab/beanscope/) |
| 45 | **Bean 生命周期顺序：为什么 @PostConstruct 里事务代理还没生效？** | 在 @PostConstruct 里调自己的 @Transactional 方法预热数据，或者在初始化前访问注入字段——不是 NPE 就是代理没包上！ | `lab.lifecycle` | [网页课件](./pages/spring-bean-lifecycle.html) · [JSON](./lessons/spring-bean-lifecycle.json) · [源码](./src/lab/lifecycle/) |
| 46 | **事务方法里开多线程 / @Async，子线程异常主线程怎么不回滚？** | @Transactional 方法里新开线程或调 @Async 写明细表，子线程报错了，主线程却照常 commit——订单有头无尾！ | `lab.txthread` | [网页课件](./pages/spring-tx-multithread.html) · [JSON](./lessons/spring-tx-multithread.json) · [源码](./src/lab/txthread/) |
| 47 | **Spring Boot 自动装配：@ConditionalOnMissingBean 为什么有时会失效？** | 自己配了一个 Serializer Bean 想覆盖默认实现，结果要么启动直接报 NoUniqueBeanDefinitionException，要么默认实现抢先占坑！ | `lab.autowire` | [网页课件](./pages/spring-boot-autowire.html) · [JSON](./lessons/spring-boot-autowire.json) · [源码](./src/lab/autowire/) |


### 模块 6：Redis 缓存、MQ 与微服务基础（#M6-01～#M6-06）

纯 JDK 零依赖仿真实验室：直击缓存穿透、击穿、雪崩、双写一致性、热点 Key 倾斜与 BigKey/Pipeline 代价。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M6-01 | **不存在的 ID 疯狂查库：缓存穿透怎么挡？** | 恶意或异常请求专查根本不存在的 ID，缓存与数据库双双未命中，每次都直穿到底层 DB。 | `lab.m6.penetration` | [网页课件](./pages/m6-cache-penetration.html) · [JSON](./lessons/m6-cache-penetration.json) · [源码](./src/lab/m6/penetration/) |
| M6-02 | **热点 Key 过期瞬间：互斥锁 vs 逻辑过期** | 某个爆款热点 Key 到点过期的一瞬间，成百上千并发请求同时 miss 挤向数据库，到底该排队等锁还是直接返回旧值？ | `lab.m6.breakdown` | [网页课件](./pages/m6-cache-breakdown.html) · [JSON](./lessons/m6-cache-breakdown.json) · [源码](./src/lab/m6/breakdown/) |
| M6-03 | **整点批量过期：为什么缓存雪崩打垮库？** | 初始化缓存时给大量 Key 统一设死 TTL，到点集体失效，数据库峰值 QPS 瞬间打穿安全线。 | `lab.m6.avalanche` | [网页课件](./pages/m6-cache-avalanche.html) · [JSON](./lessons/m6-cache-avalanche.json) · [源码](./src/lab/m6/avalanche/) |
| M6-04 | **先删缓存还是先改库？并发读写脏数据现场** | 更新数据时先删缓存还是先改库？用并发实验亲眼看看旧值是怎么被读线程倒灌回缓存的。 | `lab.m6.consistency` | [网页课件](./pages/m6-cache-consistency.html) · [JSON](./lessons/m6-cache-consistency.json) · [源码](./src/lab/m6/consistency/) |
| M6-05 | **顶流热点 Key 打爆单分片：本地二级缓存与 Key 拆分** | 就算 Redis 搭了 16 个分片集群，某一个顶流热搜 Key 还是只会落到其中 1 个分片上，把单分片打满。 | `lab.m6.hotkey` | [网页课件](./pages/m6-hotkey-split.html) · [JSON](./lessons/m6-hotkey-split.json) · [源码](./src/lab/m6/hotkey/) |
| M6-06 | **BigKey 阻塞单线程 + Pipeline 批量往返代价** | 一次查 40 条数据，循环单发和 Pipeline 差多少？大 Key 又是怎么把单线程卡死的？ | `lab.m6.bigkey` | [网页课件](./pages/m6-bigkey-pipeline.html) · [JSON](./lessons/m6-bigkey-pipeline.json) · [源码](./src/lab/m6/bigkey/) |

### 模块八：笔试算法 + SQL 手撕（M8-01～M8-10 · 10 组专题 · 101 题）

覆盖笔试与一二面高频手撕题：先用实测数据与 `javap` 字节码打破直觉误区，再对照 [101 题高频刷题清单](./docs/module8-100-problems.md) 与 [SQL 20 题可执行脚本](./src/lab/sqlrank/queries.sql) 每日手敲。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M8-01 | **两数之和暴力跑了多久？哈希与双指针怎么省出 100 倍** | O(n²) 暴力在 6 万数据下直接慢成幻灯片，预分配 HashMap 怎么几乎 0 毫秒？ | `lab.hashtwo` | [网页课件](./pages/hash-twopointer.html) · [JSON](./lessons/hash-twopointer.json) · [源码](./src/lab/hashtwo/) |
| M8-02 | **链表翻转与环检测：递归长链会爆栈，三指针怎么原地零分配** | 10 万节点链表递归直接 StackOverflowError，三指针与 dummy 哨兵怎么零分配稳过？ | `lab.linkedlist` | [网页课件](./pages/linked-list.html) · [JSON](./lessons/linked-list.json) · [源码](./src/lab/linkedlist/) |
| M8-03 | **栈与单调队列：滑动窗口最大值怎么从 O(nk) 压到均摊 O(n)** | 窗口右移一格，为什么暴力扫和 TreeMap 都输给了 ArrayDeque 单调队列？ | `lab.stackqueue` | [网页课件](./pages/stack-queue.html) · [JSON](./lessons/stack-queue.json) · [源码](./src/lab/stackqueue/) |
| M8-04 | **二叉树递归与层序：偏斜树为什么把递归打爆，队列 size 怎么锁死每一层** | 3 万层偏斜树下递归直接爆栈，显式栈与 Morris 怎么稳如磐石？ | `lab.bintree` | [网页课件](./pages/binary-tree.html) · [JSON](./lessons/binary-tree.json) · [源码](./src/lab/bintree/) |
| M8-05 | **二分搜索与边界：为什么总写死循环，(l + r) / 2 怎么就溢出了** | 百万数据二分只需 20 步，差一个等号或右移符号怎么就漏解或死循环？ | `lab.binsearch` | [网页课件](./pages/binary-search.html) · [JSON](./lessons/binary-search.json) · [源码](./src/lab/binsearch/) |
| M8-06 | **回溯排列与组合：忘掉撤销现场会全串，剪枝去重怎么省出 10 倍时间** | 每次拷贝路径慢如蜗牛，忘掉 remove 会全串，排序同层去重怎么提速？ | `lab.backtrack` | [网页课件](./pages/backtrack.html) · [JSON](./lessons/backtrack.json) · [源码](./src/lab/backtrack/) |
| M8-07 | **动态规划与状态压缩：纯递归怎么指数级卡死，滚动变量怎么压到 O(1)** | 第 40 阶楼梯纯递归飙出数百毫秒，记忆化与滚动变量一万遍只要一眨眼？ | `lab.dpbasics` | [网页课件](./pages/dp-basics.html) · [JSON](./lessons/dp-basics.json) · [源码](./src/lab/dpbasics/) |
| M8-08 | **手写线程安全 LRU（接第 7 课）：为什么 readLock 照样崩，分段锁怎么提速** | get() 也会改双向链表，加 readLock 照样崩；16 分段锁在 8 线程下怎么拉开差距？ | `lab.tslru` | [网页课件](./pages/threadsafe-lru.html) · [JSON](./lessons/threadsafe-lru.json) · [源码](./src/lab/tslru/) |
| M8-09 | **多线程交替打印：wait 不包在 while 里怎么就乱序了，Condition 怎么精准接力** | 三线程交替打印 ABC：while 换成 if 立马乱套，Condition 与 Semaphore 怎么稳控？ | `lab.altprint` | [网页课件](./pages/alt-print.html) · [JSON](./lessons/alt-print.json) · [源码](./src/lab/altprint/) |
| M8-10 | **高频分组排名 SQL 20 题：为什么窗口函数比相关子查询快一个数量级** | 部门工资前三高，相关子查询 O(n²) 慢爆，DENSE_RANK 怎么一趟搞定？ | `lab.sqlrank` | [网页课件](./pages/sql-ranking.html) · [JSON](./lessons/sql-ranking.json) · [源码](./src/lab/sqlrank/) |

## 本地动手运行实验代码

仓库中的所有示例仅使用 JDK 标准库，无任何第三方依赖（第 31 课 `virtual-race` 需 **JDK 21+**，其余课程兼容常见 JDK；全部实验数据实测于 **JDK 25**）。

在仓库根目录下编译并运行任意一课（以第 3 课 `lab.tally` 为例）：

```bash
# 1. 编译该课的所有变体实现
javac -encoding UTF-8 -d out src/lab/tally/*.java

# 2. 以 4 个并发线程运行不同写法对比结果
java -cp out lab.tally.Plain 4
java -cp out lab.tally.SafeMap 4
java -cp out lab.tally.Sync 4
java -cp out lab.tally.Merge 4
```

## 开源协议

本项目采用 [MIT License](./LICENSE) 开源。
