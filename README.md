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
├── index.html   # 32 节实验课总导航入口页（支持搜索、分章筛选与亮暗主题）
├── pages/       # 32 份单文件交互式课件网页（双击即开，零外链依赖）
├── lessons/     # 32 份课程结构与讲解数据（JSON）
├── src/         # 128 份可独立编译运行的 Java 对照源码（每课 4 种变体）
└── results/     # 实验台在本机实测沉淀的运行数据与 javap 输出记录（JSON）
```

## 完整课表（共 32 课）

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

### 模块四 · 第一章：SQL 必知必会、联表与深分页改写（第 33～37 课）

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 33 | **循环查库 N+1，接口怎么慢成蜗牛？** | 先查出 1000 条订单，再在 for 循环里一条条查下单人名字。每次只查 1 行，接口为什么还是慢到超时？ | `lab.sqljoin` | [网页课件](./pages/sql-join-nplus1.html) · [JSON](./lessons/sql-join-nplus1.json) · [源码](./src/lab/sqljoin/) |
| 34 | **翻到第 10 万页，LIMIT 为啥突然卡死？** | 同样每页只取 20 条，LIMIT 0, 20 只要 1 毫秒，LIMIT 100000, 20 为什么突然变成慢查询？ | `lab.deeppage` | [网页课件](./pages/sql-deep-paging.html) · [JSON](./lessons/sql-deep-paging.json) · [源码](./src/lab/deeppage/) |
| 35 | **COUNT(*) 到底比 COUNT(1) 慢多少？** | 面试常问 COUNT(*)、COUNT(1)、COUNT(列名) 谁最快。用错不仅慢，COUNT(列名) 还会把统计数算少！ | `lab.countstar` | [网页课件](./pages/sql-count-star.html) · [JSON](./lessons/sql-count-star.json) · [源码](./src/lab/countstar/) |
| 36 | **WHERE 和 HAVING 混用，全表都被扫了一遍？** | 同样是筛选 status = 'PAID'，写在 WHERE 里只要 2 毫秒，写在 HAVING 里为什么把临时表撑爆？ | `lab.wherehaving` | [网页课件](./pages/sql-group-having.html) · [JSON](./lessons/sql-group-having.json) · [源码](./src/lab/wherehaving/) |
| 37 | **字段允许 NULL，!= 怎么把数据全漏掉了？** | 用 WHERE id NOT IN (SELECT ...) 查黑名单外用户，子查询里只混进了一个 NULL，整条 SQL 竟然返回 0 行！ | `lab.nulltrap` | [网页课件](./pages/sql-null-trap.html) · [JSON](./lessons/sql-null-trap.json) · [源码](./src/lab/nulltrap/) |

### 模块五 · 第一章：Spring 事务失效、动态代理与 AOP 切面（第 38～42 课）

用纯 JDK 动态代理与字节码证据，亲手复现同类自调用、异常吞没、传播行为连坐回滚、JDK/CGLIB 代理坑与多切面洋葱错序。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 38 | **同类自调用，@Transactional 为什么没回滚？** | 外层方法没加事务，内部直接 this.payAndRecord() 调了加 @Transactional 的方法。抛异常后，库存却没回滚！ | `lab.txself` | [网页课件](./pages/spring-tx-self.html) · [JSON](./lessons/spring-tx-self.json) · [源码](./src/lab/txself/) |
| 39 | **异常被吞或抛检查异常，事务怎么照样提交了？** | 明明加了 @Transactional，也确实出错了——但要么被 try-catch 吞掉，要么抛了 IOException，结果扣款照样 commit！ | `lab.txswallow` | [网页课件](./pages/spring-tx-swallow.html) · [JSON](./lessons/spring-tx-swallow.json) · [源码](./src/lab/txswallow/) |
| 40 | **外层 catch 住内层异常，整个事务怎么还是全炸了？** | 主流程加了事务，调用「加积分」子方法时用 try-catch 兜住了异常——结果提交时竟抛出 UnexpectedRollbackException，连主订单一起回滚！ | `lab.txprop` | [网页课件](./pages/spring-tx-prop.html) · [JSON](./lessons/spring-tx-prop.json) · [源码](./src/lab/txprop/) |
| 41 | **JDK 动态代理 vs CGLIB：强转就炸、final 就失效** | 有接口用 JDK 代理，强转实现类直接 ClassCastException；换成 CGLIB 子类代理，加个 final 切面就静默失效！ | `lab.proxykind` | [网页课件](./pages/spring-proxy-kind.html) · [JSON](./lessons/spring-proxy-kind.json) · [源码](./src/lab/proxykind/) |
| 42 | **多个 @Aspect 切面叠在一起：锁、日志、事务谁先谁后？** | 下单方法同时挂了分布式锁切面和 @Transactional。如果锁在事务里面——锁都放开了事务还没提交，下一个线程直接读到旧余额！ | `lab.aoporder` | [网页课件](./pages/spring-aop-order.html) · [JSON](./lessons/spring-aop-order.json) · [源码](./src/lab/aoporder/) |

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
