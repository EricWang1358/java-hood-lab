# Java Hood Lab — 掀开机盖学 Java

> **先猜后验，再看证据**：基于本机实测数据与 `javap` 字节码的交互式 Java 并发实验课。

## 学一节课

学习时请先看下面的[课程顺序](#课程顺序)，了解全系列 32 节的主题和前置顺序，再从第 1 节（[`pages/counter-race.html`](pages/counter-race.html)）开始按顺序学习。

- **在线打开（GitHub Pages）**：[https://ericwang1358.github.io/java-hood-lab/](https://ericwang1358.github.io/java-hood-lab/)
- **本地离线学习**：克隆或下载本仓库后，直接双击打开 [`index.html`](index.html) 或 [`pages/`](pages/) 下对应一节的 `.html` 文件即可浏览，不依赖外部网络资源。
- **课堂操作**：点击「下一步」或按 `→` / `空格` 推进讲解，按 `←` 回退；点击带虚线下划线的术语可以弹出解释；右上角可切换亮/暗配色与课堂笔记。

## 每节的设计闭环

1. **先猜后看**：先按直觉选一个预估结果，再放出本机多轮实际运行散点做对照。
2. **掀开机盖**：结合 `javap -c -p` 字节码指令与并发交错泳道，看清多步之间从哪里被插队。
3. **权衡代价**：在不同并发线程数下对比几种改法的真实耗时折线与正确率，总结选型规则并做题核对。
4. **复述带走**：2 分钟自述检验重点，并整理最多 3 张复习卡。

## 课程顺序

第 1、2 节固定从共享计数和库存扣减起步，随后依次经过并发容器、锁边界与读写协同、无锁 CAS 与不可变快照、线程封闭与异步编排，再到后半段的工程并发模式与虚拟线程（注：高并发工程场景均采用内存级小型模型演示单机核心竞争点，真实现场需配合数据库事务、分布式锁或消息重试机制）。

| 序号 | 课 id | 标题 | 页面 | 一句话讲什么 |
|---|---|---|---|---|
| 1 | `counter-race` | 两个线程一起数数，能数对吗 | [pages/counter-race.html](pages/counter-race.html) | 多线程对同一个 `static` 整数循环加一，对照普通计数、`volatile`、`synchronized` 和 `AtomicInteger`。 |
| 2 | `stock-oversell` | 库存只有这么多，会卖超吗 | [pages/stock-oversell.html](pages/stock-oversell.html) | 多线程先查库存再扣一件，比较普通写法、`AtomicInteger`、加锁与 `CAS` 循环的差异。 |
| 3 | `tally-map` | 几个线程一起按商品记销量，账对得上吗？ | [pages/tally-map.html](pages/tally-map.html) | 多个线程往同一张 `Map` 里按商品累加销量，对比 `ConcurrentHashMap` 先取再放、加锁与 `merge`。 |
| 4 | `absent-init` | 几个线程同时读冷缓存，连接会建几次？ | [pages/absent-init.html](pages/absent-init.html) | 缓存缺失时现建连接，对比普通先查后放、`putIfAbsent`、全方法加锁与 `computeIfAbsent`。 |
| 5 | `list-append` | 几个线程往同一列表追加日志，条数对得上吗？ | [pages/list-append.html](pages/list-append.html) | 预留足容量的共享列表被多线程并发追加，对比普通列表、`volatile` 列表、同步列表与并发队列。 |
| 6 | `cow-iter` | 一边遍历白名单一边偶尔更新，能顺利读完吗？ | [pages/cow-iter.html](pages/cow-iter.html) | 读多写少场景下多线程扫描白名单同时偶发增删，对比普通列表、同步列表、加锁遍历与写时复制。 |
| 7 | `lru-visit` | 几个线程只从最近访问缓存里读数据，会出事吗？ | [pages/lru-visit.html](pages/lru-visit.html) | 开启按访问排序的 `LinkedHashMap` 只调 `get` 也会改链表，对比安全 `Map`、`readLock` 与 `writeLock`。 |
| 8 | `lock-scope` | 每次新建对象去调记账方法，总账对得上吗？ | [pages/lock-scope.html](pages/lock-scope.html) | 总账记在静态字段而 `synchronized` 锁住每次新建的 `this` 实例，对比锁范围与类锁。 |
| 9 | `box-lock` | 把共享计数器写成包装类，几个线程能数准吗？ | [pages/box-lock.html](pages/box-lock.html) | 用会因自动装箱而换对象的静态 `Integer` 当锁监视器，对比固定锁对象与原子类。 |
| 10 | `rw-lock` | 原价和折扣成对修改，并发查价会读到什么？ | [pages/rw-lock.html](pages/rw-lock.html) | 原价和折扣需成对维持比例，读远多于写时对比双 `volatile`、互斥锁与读写锁。 |
| 11 | `stamped-opt` | 一边改双格坐标一边高频读取，读到的是一对吗？ | [pages/stamped-opt.html](pages/stamped-opt.html) | 高频读双格坐标配合偶发写，对比 `StampedLock` 先验后读、先读后验乐观读与读写锁。 |
| 12 | `deadlock-order` | 几个线程在账户之间互相转账，总账能平吗？ | [pages/deadlock-order.html](pages/deadlock-order.html) | 两账户互转需要同时持有两把锁，对比无锁、按参数顺序加锁、固定全序加锁与单把大锁。 |
| 13 | `cas-aba` | 账户余额变过又回到原值，首充补贴会发几次？ | [pages/cas-aba.html](pages/cas-aba.html) | 余额从原值改写后又回到原值，对比单值 `CAS`、全方法加锁与带版本号的 `AtomicStampedReference`。 |
| 14 | `adder-limit` | 用分段累加器统计已发券数，能卡住上限吗？ | [pages/adder-limit.html](pages/adder-limit.html) | 高并发下卡每波优惠券上限，对比 `LongAdder` 先求和再自增、整段加锁与 `CAS` 自旋限发。 |
| 15 | `multi-ref` | 门槛和减免额成套热更新，接单会读到哪一套？ | [pages/multi-ref.html](pages/multi-ref.html) | 折扣与门槛需成套切换并校验，对比双 `AtomicInteger`、加锁与不可变对象配 `AtomicReference`。 |
| 16 | `date-format` | 几个线程共用日期格式化工具，转出的日子准吗？ | [pages/date-format.html](pages/date-format.html) | 多线程并发转换固定日期，对比共用实例、按模板缓存实例、加锁与不可变 `DateTimeFormatter`。 |
| 17 | `singleton-state` | 把订单原价暂存在共享服务字段里，折后价准吗？ | [pages/singleton-state.html](pages/singleton-state.html) | 多线程共用同一服务实例却把中间金额存进成员字段，对比成员暂存、`volatile`、加锁与局部参数。 |
| 18 | `threadlocal-pool` | 固定线程池轮流接待请求，用户身份会留底吗？ | [pages/threadlocal-pool.html](pages/threadlocal-pool.html) | 线程池复用线程时用 `ThreadLocal` 存身份，遇到异常分支对比不清理、仅普通路径清理、`finally` 清理与按单号查表。 |
| 19 | `tl-inherit` | 主线程存好的追踪编号，进了线程池还读得到吗？ | [pages/tl-inherit.html](pages/tl-inherit.html) | 父线程的 `ThreadLocal` 追踪号随子任务进固定线程池，对比普通、可继承、每次新建线程与显式快照传参。 |
| 20 | `pool-reject` | 有界线程池与队列都满时，多出来的任务去哪了？ | [pages/pool-reject.html](pages/pool-reject.html) | 有界工作线程与有界队列满载后，对比默认拒收吞错、静默丢弃、全局串行与调用者反压策略。 |
| 21 | `cf-combine` | 几个异步分支算完汇总到同一本账，结果准吗？ | [pages/cf-combine.html](pages/cf-combine.html) | 多路异步任务用 `allOf` 汇合，对比回调改共享表、汇合后再算、加锁写入与只返回值由主线程归并。 |
| 22 | `semaphore-gate` | 下游最多同时接两单，八路请求涌来会放进几个？ | [pages/semaphore-gate.html](pages/semaphore-gate.html) | 模拟下游同时最多承受两单，对比先查原子计数再放行、单通道互斥锁与 `Semaphore` 信号量闸门。 |
| 23 | `once-per-user` | 每人限领一张优惠券，同一账号连点会发出几张？ | [pages/once-per-user.html](pages/once-per-user.html) | 模拟同一账号并发连点领券，对比先查后记、局部拼字符串加锁、全局锁与用户键 `ConcurrentHashMap` 原子方法。 |
| 24 | `order-state` | 支付回调与超时关单同时到达，订单会落向哪边？ | [pages/order-state.html](pages/order-state.html) | 待支付订单同时收到支付与超时事件，对比普通状态字段、`volatile` 判断、全锁与 `CAS` 状态迁移。 |
| 25 | `cache-aside` | 缓存刚清空就涌来并发查询，数据库会被查几次？ | [pages/cache-aside.html](pages/cache-aside.html) | 缓存清空后面对并发冷查询，对比无保护回源、判空查库后 `putIfAbsent`、全锁与按键单飞归并。 |
| 26 | `sharded-stock` | 把八百件库存拆进十六个格子卖，最后会卖出多少？ | [pages/sharded-stock.html](pages/sharded-stock.html) | 在内存模拟中把一份库存拆成 16 个槽位并允许顺延扣减，对比单把大锁、普通整数分片与分段原子 `CAS`。 |
| 27 | `id-gen` | 时间窗配序列号一起发号，跨窗瞬间会重号吗？ | [pages/id-gen.html](pages/id-gen.html) | 模拟时间窗与序列号协同递增发号，对比分开的两组原子字段、全局锁与单 `long` 位打包 `CAS`。 |
| 28 | `compound-avg` | 一边累加次数与总耗时一边算均值，结果准吗？ | [pages/compound-avg.html](pages/compound-avg.html) | 统计总次数与总耗时并实时求比值，对比双原子字段、加锁与不可变统计快照 `CAS`。 |
| 29 | `lazy-singleton` | 多个线程同时首次获取懒加载对象，会建出几个？ | [pages/lazy-singleton.html](pages/lazy-singleton.html) | 多线程同时首次懒加载初始化共享对象，对比普通判空、单检加锁、全方法加锁与 `volatile` 双检锁。 |
| 30 | `queue-drain` | 一边往队列写日志一边整批取走，条数对得上吗？ | [pages/queue-drain.html](pages/queue-drain.html) | 生产线程连续入队、消费线程定期批量取走清空，对比先快照再 `clear`、互斥加锁与 `drainTo` 原子抽干。 |
| 31 | `virtual-race` | 把平台线程换成虚拟线程，一起数数能数对吗？ | [pages/virtual-race.html](pages/virtual-race.html) | 只把线程从平台线程换成 `Thread.ofVirtual()` 跑共有计数，对比无同步、加锁与并发累加。 |
| 32 | `tx-outbox` | 改完主数据再异步刷新查询表，两边最后一致吗？ | [pages/tx-outbox.html](pages/tx-outbox.html) | 在内存数组模拟主写后异步投影的乱序覆写，对比无锁双写、双原子数组、全局锁与按版本号覆盖（模拟可靠投递后下游按版本丢弃旧消息这一半）。 |

## 目录结构

```text
.
├── index.html   # 32 节实验导航入口页（支持搜索、分章筛选与亮暗主题）
├── pages/       # 32 份单文件交互式课件网页（双击即开，零外链依赖）
├── lessons/     # 32 份课程结构与讲解数据（JSON）
├── src/         # 128 份可独立编译运行的 Java 对照源码（每节 4 种变体）
└── results/     # 实验台在本机实测沉淀的运行数据与 javap 输出记录（JSON）
```

## 本地动手运行实验代码

仓库中的示例仅使用 JDK 标准库，不引入第三方依赖（第 31 节 `virtual-race` 需 **JDK 21+**，其余各节兼容常见 JDK；全部实验数据实测于 **JDK 25**）。

在仓库根目录下编译并运行任意一节（以第 3 节 `lab.tally` 为例）：

```bash
# 1. 编译该节的所有变体实现
javac -encoding UTF-8 -d out src/lab/tally/*.java

# 2. 以 4 个并发线程运行不同写法对比结果
java -cp out lab.tally.Plain 4
java -cp out lab.tally.SafeMap 4
java -cp out lab.tally.Sync 4
java -cp out lab.tally.Merge 4
```

## 开源协议

本项目采用 [MIT License](./LICENSE) 开源。
