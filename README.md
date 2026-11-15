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

### 模块一 · 第一章：Java 值传递、包装类缓存与精度陷阱（M1-01～M1-05）

用真实字节码与实测数据拆穿「对象引用传递」、Integer 128 翻车、BigDecimal 算钱三连坑、循环 `+` 拼接与浅拷贝污染。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M1-01 | **值传递与引用副本：方法里 swap 或重赋值为什么外面没变？** | 大家都说「Java 对象是引用传递」，可为什么在方法里写 box = new Box(99) 之后，调用方的 box.val 还是 0？ | `lab.passbyval` | [网页课件](./pages/java-pass-by-value.html) · [JSON](./lessons/java-pass-by-value.json) · [源码](./src/lab/passbyval/) |
| M1-02 | **包装类缓存池与拆箱陷阱：为什么 Integer == Integer 127 对 128 错？** | 测试环境用户 ID 从 1 到 100 跑得好好的，一上线 ID 涨到 128 以上，用 == 判断两个 Integer 相等突然全变 false！ | `lab.intcache` | [网页课件](./pages/java-integer-cache.html) · [JSON](./lessons/java-integer-cache.json) · [源码](./src/lab/intcache/) |
| M1-03 | **BigDecimal 算钱三连坑：double 构造、equals 标度与舍入模式** | 不用 double 算钱改用 BigDecimal 了，可为什么 new BigDecimal(0.1) 还是算不准，equals 比较 0.3 和 0.30 竟然返回 false？ | `lab.bigdecimal` | [网页课件](./pages/java-bigdecimal.html) · [JSON](./lessons/java-bigdecimal.json) · [源码](./src/lab/bigdecimal/) |
| M1-04 | **String 不可变性与循环拼接：为什么循环里用 + 会把 CPU 拖死？** | 面经说 JDK 9+ 字符串 + 会优化成 invokedynamic，那在 for 循环里写 s += "a" 拼 2 万次，还会慢成 O(N²) 吗？ | `lab.strimmut` | [网页课件](./pages/java-string-immut.html) · [JSON](./lessons/java-string-immut.json) · [源码](./src/lab/strimmut/) |
| M1-05 | **浅拷贝污染事故：改了副本，原对象的数组/列表怎么也跟着变了？** | 好不容易实现了 Cloneable 并调用 super.clone() 复制出一个用户副本，可一改副本的 scores[0]，原用户的分数竟然也被改掉了！ | `lab.deepcopy` | [网页课件](./pages/java-deep-copy.html) · [JSON](./lessons/java-deep-copy.json) · [源码](./src/lab/deepcopy/) |

### 模块一 · 第二章：equals/hashCode、不可变类、泛型与枚举（M1-06～M1-10）

实测 HashMap 丢键与退化、不可变防御拷贝、泛型擦除堆污染、Comparator 减法溢出与 EnumMap 数组直寻址。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M1-06 | **equals 与 hashCode 契约：为什么只重写 equals 会让 HashMap 丢键？** | 明明两个 Key 的 id 完全一样、equals() 也返回 true，为什么 put 进 HashMap 后再 get(new Key(id)) 竟然返回 null？ | `lab.eqhash` | [网页课件](./pages/java-equals-hashcode.html) · [JSON](./lessons/java-equals-hashcode.json) · [源码](./src/lab/eqhash/) |
| M1-07 | **不可变类设计：final 字段加上 private，为什么 List 还是被外面改坏了？** | 类加了 final、字段也加了 private final List<String> roles，可为什么外面调用方往原 list 里 add 一条，配置竟然被悄悄篡改？ | `lab.immut` | [网页课件](./pages/java-immutable-class.html) · [JSON](./lessons/java-immutable-class.json) · [源码](./src/lab/immut/) |
| M1-08 | **泛型擦除、堆污染与 PECS 原则：运行期 List<Integer> 里怎么塞进了 String？** | 明明声明的是 List<Integer>，为什么通过原始类型引用塞进一个 "bad-string" 竟然编译运行都不报错，直到取出那一刻才炸出 ClassCastException？ | `lab.generics` | [网页课件](./pages/java-generics-erasure.html) · [JSON](./lessons/java-generics-erasure.json) · [源码](./src/lab/generics/) |
| M1-09 | **Comparator 减法溢出坑与 TreeSet 神秘吞元素** | 图省事写了个 (a, b) -> a - b 排序，遇到负数直接排反；更诡异的是往 TreeSet 里加 1000 个不同 ID 的同分元素，size() 竟然只有 1！ | `lab.compsort` | [网页课件](./pages/java-comparable-sort.html) · [JSON](./lessons/java-comparable-sort.json) · [源码](./src/lab/compsort/) |
| M1-10 | **Enum 枚举本质、EnumMap 加速与状态机模式** | 为什么《Effective Java》盛赞 Enum 是最佳单例？把订单状态存在 EnumMap 里，比 HashMap<OrderState, String> 和魔法数字快在哪里？ | `lab.enumstate` | [网页课件](./pages/java-enum-state.html) · [JSON](./lessons/java-enum-state.json) · [源码](./src/lab/enumstate/) |

### 模块一 · 第三章：异常机制、反射注解、SPI 与重载分派（M1-11～M1-16）

实测异常爬栈开销、`try-with-resources` 抑制异常、反射/`MethodHandle` 性能梯度、注解缓存、SPI 与变长参数分派。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M1-11 | **异常栈捕获代价：为什么不能用 try-catch 代替 if-else 控制业务流？** | 用异常代替返回 false 或错误码，表面代码少写了几个 if，可一旦并发上来，为什么 CPU 全烧在 Throwable.fillInStackTrace() 上？ | `lab.exccost` | [网页课件](./pages/java-exception-cost.html) · [JSON](./lessons/java-exception-cost.json) · [源码](./src/lab/exccost/) |
| M1-12 | **try-with-resources 防泄漏与抑制异常：finally 怎么把主异常吞掉了？** | 用老式 try-finally 手动关流，结果 work() 抛了核心业务异常，finally 里的 close() 又抛了一个异常——外层 catch 竟然只看到 close() 的报错！ | `lab.tryres` | [网页课件](./pages/java-try-resources.html) · [JSON](./lessons/java-try-resources.json) · [源码](./src/lab/tryres/) |
| M1-13 | **反射性能真相：每次 getMethod、缓存 setAccessible 与 MethodHandle 差多少？** | 大家都说「Java 反射慢」，可 Spring/MyBatis/Jackson 到处都在用反射。到底慢在查元数据、权限检查，还是 invoke 本身？ | `lab.reflect` | [网页课件](./pages/java-reflection-cost.html) · [JSON](./lessons/java-reflection-cost.json) · [源码](./src/lab/reflect/) |
| M1-14 | **注解 Retention 生命周期与运行时解析缓存** | 自定义了一个权限注解加在接口上，为什么运行时 getAnnotation() 竟然返回 null？如果每次请求都反射读注解，又会多出多少开销？ | `lab.annocache` | [网页课件](./pages/java-annotation-proc.html) · [JSON](./lessons/java-annotation-proc.json) · [源码](./src/lab/annocache/) |
| M1-15 | **SPI 扩展机制与懒加载缓存：JDBC / Dubbo / Spring Boot 是怎么插拔实现的？** | 写一个核心接口，不改核心包一行代码，只往 classpath 放个 jar 就能自动加载实现类——JDK ServiceLoader 怎么做到的？为什么每次重新 load 这么慢？ | `lab.spiloader` | [网页课件](./pages/java-spi-loader.html) · [JSON](./lessons/java-spi-loader.json) · [源码](./src/lab/spiloader/) |
| M1-16 | **变长参数数组分配、重载静态分派与装箱优先级** | 写了个便捷方法 sum(int... nums)，在热循环里每次传 2 个参数竟然都在偷偷 new int[2]！更迷惑的是 Base ref = new Sub(); 调重载方法 tag(ref) 到底走哪个？ | `lab.varargs` | [网页课件](./pages/java-varargs-overload.html) · [JSON](./lessons/java-varargs-overload.json) · [源码](./src/lab/varargs/) |

### 模块一 · 第四章：集合底层原理与 Stream 陷阱（M1-17～M1-20）

实测 ArrayList 扩容 vs LinkedList 随机访问、fail-fast 与 `removeIf`、HashMap 预分配容量与 8/64 红黑树化。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M1-17 | **ArrayList 1.5 倍扩容 vs LinkedList：随机访问慢在哪？** | 面试都说「LinkedList 适合增删、ArrayList 适合查询」，可为什么用 for(i) 调 LinkedList.get(i) 遍历 4 万条数据直接卡成几百毫秒？ | `lab.arraygrow` | [网页课件](./pages/col-arraylist-grow.html) · [JSON](./lessons/col-arraylist-grow.json) · [源码](./src/lab/arraygrow/) |
| M1-18 | **fail-fast 快速失败：为什么 for-each 里删元素会炸 ConcurrentModificationException？** | 单线程好端端地遍历一个 ArrayList，顺手删掉偶数元素就立刻炸出 ConcurrentModificationException；换成下标正序删又不报错了，结果竟悄悄漏删了一半！ | `lab.failfast` | [网页课件](./pages/col-fail-fast.html) · [JSON](./lessons/col-fail-fast.json) · [源码](./src/lab/failfast/) |
| M1-19 | **HashMap 初始容量与负载因子：new HashMap<>(10000) 为什么还是会扩容？** | 明明知道要往 HashMap 里放 10000 条数据，贴心地写了 new HashMap<>(10000)，可源码算下来竟然还是在放第 7501 条时触发了一次全表扩容！ | `lab.mapresize` | [网页课件](./pages/col-hashmap-resize.html) · [JSON](./lessons/col-hashmap-resize.json) · [源码](./src/lab/mapresize/) |
| M1-20 | **HashMap 哈希碰撞与红黑树化：为什么 8 和 64 两个阈值缺一不可？** | 故意让 4000 个 Key 的 hashCode 全部撞进同一个桶，为什么 Key 实现 Comparable 时查询快如闪电，没实现 Comparable 时却慢成几十倍？ | `lab.treeify` | [网页课件](./pages/col-hashmap-treeify.html) · [JSON](./lessons/col-hashmap-treeify.json) · [源码](./src/lab/treeify/) |

### 模块二 · 第一章：类加载、初始化与栈堆分配（M2-01～M2-04）

用真实 `ClassLoader` 隔离、`<clinit>` 死锁、栈帧深度爆栈与 Eden TLAB 并发分配实测 JVM 底层运行机制。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M2-01 | **双亲委派与打破：同名类怎么加载出两份？** | JDBC SPI、Tomcat 多应用隔离和热部署为什么要打破双亲委派？重写 findClass 与 loadClass 差在哪？ | `lab.classload` | [网页课件](./pages/jvm-classload.html) · [JSON](./lessons/jvm-classload.json) · [源码](./src/lab/classload/) |
| M2-02 | **静态初始化顺序与 <clinit> 死锁** | 静态变量和 static{} 谁先执行？两个类在静态初始化时互相调用为什么会触发隐蔽的 <clinit> 死锁？ | `lab.clinit` | [网页课件](./pages/jvm-clinit.html) · [JSON](./lessons/jvm-clinit.json) · [源码](./src/lab/clinit/) |
| M2-03 | **虚拟机栈帧与 StackOverflow** | 同样递归 10000 层，为什么局部变量多写几个 long 就会提前炸出 StackOverflowError？ | `lab.jvmstack` | [网页课件](./pages/jvm-stack.html) · [JSON](./lessons/jvm-stack.json) · [源码](./src/lab/jvmstack/) |
| M2-04 | **TLAB 线程本地分配缓冲** | 堆内存明明所有线程共享，为什么多线程同时 new 对象不抢同一把锁？ | `lab.tlab` | [网页课件](./pages/jvm-tlab.html) · [JSON](./lessons/jvm-tlab.json) · [源码](./src/lab/tlab/) |

### 模块二 · 第二章：对象头、逃逸分析、引用与 GC 机制（M2-05～M2-09）

实测 `synchronized` 锁升级膨胀、JIT 标量替换、强软弱虚引用回收时机、GC Roots 循环孤岛与大对象直入老年代停顿。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M2-05 | **对象头 MarkWord 与锁升级** | 对象头里 64 bit 存了什么？无竞争轻量级锁与多线程膨胀重量级锁耗时差多少？ | `lab.markword` | [网页课件](./pages/jvm-markword.html) · [JSON](./lessons/jvm-markword.json) · [源码](./src/lab/markword/) |
| M2-06 | **逃逸分析与标量替换** | 循环里 new 百万个临时对象为什么和直接写 int 一样快？ | `lab.escape` | [网页课件](./pages/jvm-escape.html) · [JSON](./lessons/jvm-escape.json) · [源码](./src/lab/escape/) |
| M2-07 | **强软弱虚引用与 ThreadLocal 泄漏** | 调一次 System.gc() 后强软弱虚引用各自收多少？ThreadLocal 为什么还会内存泄漏？ | `lab.references` | [网页课件](./pages/jvm-references.html) · [JSON](./lessons/jvm-references.json) · [源码](./src/lab/references/) |
| M2-08 | **GC Roots 可达性分析** | A↔B 循环引用为什么不会内存泄漏？哪些对象能当 GC Roots？ | `lab.gcroots` | [网页课件](./pages/jvm-gcroots.html) · [JSON](./lessons/jvm-gcroots.json) · [源码](./src/lab/gcroots/) |
| M2-09 | **分代回收与 GC 停顿** | 朝生夕灭的小对象秒收，1MB 大数组直奔老年代怎么把 GC 耗时拖垮？ | `lab.gcpause` | [网页课件](./pages/jvm-gc-pause.html) · [JSON](./lessons/jvm-gc-pause.json) · [源码](./src/lab/gcpause/) |

### 模块二 · 第三章：常量池、OOM 复现与线上排障实战（M2-10～M2-14）

实测 StringTable 去重与高基数坑、无界缓存堆 OOM、动态代理类元空间爆炸、CPU 100% 正则/死循环定位与 ThreadMXBean 死锁检测。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M2-10 | **StringTable 常量池与 intern()** | JDK 7 搬进堆里后，为什么低基数词 intern() 省内存而随机 UUID 调 intern() 会拖垮服务？ | `lab.stringtable` | [网页课件](./pages/jvm-string-table.html) · [JSON](./lessons/jvm-string-table.json) · [源码](./src/lab/stringtable/) |
| M2-11 | **堆内存 OOM 与泄漏排查** | 同样报 Java heap space，如何区分真不够用与无界 Map 慢性泄漏？ | `lab.heapoom` | [网页课件](./pages/jvm-heap-oom.html) · [JSON](./lessons/jvm-heap-oom.json) · [源码](./src/lab/heapoom/) |
| M2-12 | **元空间 Metaspace OOM** | 堆内存还剩一半，动态代理类无限生成怎么把容器内存吃穿？ | `lab.metaspace` | [网页课件](./pages/jvm-metaspace.html) · [JSON](./lessons/jvm-metaspace.json) · [源码](./src/lab/metaspace/) |
| M2-13 | **线上 CPU 100% 飙高排障** | 如何用 top -Hp + printf %x + jstack 三连招，10 秒内揪出死循环与 ReDoS 正则？ | `lab.cpudiag` | [网页课件](./pages/jvm-cpu-diag.html) · [JSON](./lessons/jvm-cpu-diag.json) · [源码](./src/lab/cpudiag/) |
| M2-14 | **线上卡死与死锁排查** | CPU 很低接口全超时，如何用 jstack -l 与 ThreadMXBean 一秒查出死锁并根治？ | `lab.deadlockdiag` | [网页课件](./pages/jvm-deadlock-diag.html) · [JSON](./lessons/jvm-deadlock-diag.json) · [源码](./src/lab/deadlockdiag/) |

### 模块三 · 第一章：TCP 传输与 Socket 底层（M3-01～M3-05）

用真实 Socket 实测三次握手建连成本、`TIME_WAIT` 端口复用、全连接队列 `backlog` 拥堵、TCP 粘包拆包与 Nagle 延迟 ACK 撞车。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M3-01 | **短连接每次都要三次握手，有多慢？** | 同样发 200 次小请求，每次新建 Socket 挥手断开 vs 复用长连接，耗时差多少？ | `lab.tcphandshake` | [网页课件](./pages/tcp-handshake.html) · [JSON](./lessons/tcp-handshake.json) · [源码](./src/lab/tcphandshake/) |
| M3-02 | **压测刚跑几千次就 BindException？** | 服务端或客户端频繁主动断开连接时，为什么端口很快被 `TIME_WAIT` 占满报错？ | `lab.timewait` | [网页课件](./pages/tcp-timewait.html) · [JSON](./lessons/tcp-timewait.json) · [源码](./src/lab/timewait/) |
| M3-03 | **accept 慢了一拍，新连接怎么直接被拒？** | 服务端 `ServerSocket` 来不及 `accept()` 时，内核全连接队列 `backlog` 装满会发生什么？ | `lab.backlog` | [网页课件](./pages/tcp-backlog.html) · [JSON](./lessons/tcp-backlog.json) · [源码](./src/lab/backlog/) |
| M3-04 | **连发 100 条消息，怎么粘成一坨还缺半截？** | TCP 是无边界的字节流，连发 100 条 `"MSG-i"`，为什么接收方按 `read(buf)` 读出来个数不对？ | `lab.sticky` | [网页课件](./pages/tcp-sticky.html) · [JSON](./lessons/tcp-sticky.json) · [源码](./src/lab/sticky/) |
| M3-05 | **小包发得越勤反而越慢？** | 连续发送小数据包并等待确认时，为什么 Nagle 算法撞上延迟 ACK 会让耗时暴涨？ | `lab.nagle` | [网页课件](./pages/tcp-nagle.html) · [JSON](./lessons/tcp-nagle.json) · [源码](./src/lab/nagle/) |

### 模块三 · 第二章：HTTP、HTTPS 与认证机制（M3-06～M3-09）

实测 HTTP/1.1 Keep-Alive 与队头阻塞、TLS 非对称握手 vs 会话复用代价、CORS 预检翻倍往返与 Session/JWT 并发鉴权取舍。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M3-06 | **关掉 Keep-Alive，接口怎么慢了这么多？** | 同样的 HTTP/1.1 接口，每次带 `Connection: close` 断开 vs 长连接复用，耗时差多少？ | `lab.keepalive` | [网页课件](./pages/http-keepalive.html) · [JSON](./lessons/http-keepalive.json) · [源码](./src/lab/keepalive/) |
| M3-07 | **换上 HTTPS，首包到底多花多少毫秒？** | 非对称签名验签 + ECDH 密钥协商 + AES-GCM 加密，TLS 握手到底贵在哪里，怎么加速？ | `lab.tlscost` | [网页课件](./pages/https-tls.html) · [JSON](./lessons/https-tls.json) · [源码](./src/lab/tlscost/) |
| M3-08 | **发个 POST 怎么先发一次 OPTIONS？** | 前端带个自定义头发跨域 POST，后端日志怎么每次收到双倍请求？CORS 预检与缓存实测。 | `lab.corspreflight` | [网页课件](./pages/http-cors.html) · [JSON](./lessons/http-cors.json) · [源码](./src/lab/corspreflight/) |
| M3-09 | **Session 查表 vs 无状态 JWT，怎么选？** | 全局锁 Session 表、`ConcurrentHashMap`、无状态 HMAC-SHA256 验签与短效缓存黑名单，性能与扩展性差多少？ | `lab.authjwt` | [网页课件](./pages/auth-jwt.html) · [JSON](./lessons/auth-jwt.json) · [源码](./src/lab/authjwt/) |

### 模块三 · 第三章：BIO、NIO 多路复用与 OS I/O（M3-10～M3-14）

实测慢连接饿死 BIO 线程池、单/多 Reactor 多路复用、`transferTo` 零拷贝、PageCache 顺序写与 Group Commit 组提交、池化 DirectBuffer 堆外内存。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M3-10 | **一连接一线程：慢客户端怎么拖垮服务端？** | 只要混进几个慢客户端，单线程 BIO 卡死、线程池被饿死、一连接一线程内存暴涨，NIO 怎么破？ | `lab.biooom` | [网页课件](./pages/bio-oom.html) · [JSON](./lessons/bio-oom.json) · [源码](./src/lab/biooom/) |
| M3-11 | **NIO Selector 多路复用：1 条线程如何扛住百并发？** | 同样面对高并发请求，BIO 线程池、单 Reactor 与主从多 Reactor 各自的性能瓶颈在哪里？ | `lab.selector` | [网页课件](./pages/nio-selector.html) · [JSON](./lessons/nio-selector.json) · [源码](./src/lab/selector/) |
| M3-12 | **零拷贝：为什么 transferTo 比传统读写快一个数量级？** | 发一个静态文件走传统 read()+write() 要经历 4 次上下文切换和 4 次内存拷贝，sendfile/transferTo 怎么砍掉一半？ | `lab.zerocopy` | [网页课件](./pages/io-zerocopy.html) · [JSON](./lessons/io-zerocopy.json) · [源码](./src/lab/zerocopy/) |
| M3-13 | **PageCache 顺序写与刷盘：fsync 为什么这么慢？** | Kafka 号称磁盘顺序写比内存随机写还快，MySQL 用组提交扛住高并发写入——秘密全在 PageCache 和 fsync 的博弈里。 | `lab.pagecache` | [网页课件](./pages/page-cache.html) · [JSON](./lessons/page-cache.json) · [源码](./src/lab/pagecache/) |
| M3-14 | **堆外内存 DirectBuffer：省了一次拷贝，为什么还可能 OOM？** | Netty 为什么默认用池化 DirectBuffer 而不是 HeapByteBuffer？每次 allocateDirect 又会踩什么大坑？ | `lab.directbuf` | [网页课件](./pages/direct-buffer.html) · [JSON](./lessons/direct-buffer.json) · [源码](./src/lab/directbuf/) |

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

### 模块四 · 第四章：四大隔离级别、MVCC 版本链与 ReadView（第 55～59 课）

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 55 | **不加隔离，怎么读到了别人回滚掉的假数据？** | 线程 A 把余额改成 900 还没提交，线程 B 读走 900 后发了货；接着线程 A 回滚成 100，钱货两空！ | `lab.dirtyread` | [网页课件](./pages/tx-dirty-read.html) · [JSON](./lessons/tx-dirty-read.json) · [源码](./src/lab/dirtyread/) |
| 56 | **同一事务里连读两次余额，怎么两次不一样？** | 同一个事务里先查余额是 100，隔了一秒再查一次竟然变成 200——这就是 RC 级别下的「不可重复读」！ | `lab.nonrepeat` | [网页课件](./pages/tx-nonrepeatable-read.html) · [JSON](./lessons/tx-nonrepeatable-read.json) · [源码](./src/lab/nonrepeat/) |
| 57 | **不加锁的快照读，怎么做到读写互不阻塞？** | 写事务正在改这一行并拿着 X 锁，为什么普通 SELECT 却一毫秒都不等直接返回旧值？掀开 ReadView 可见性算法！ | `lab.readview` | [网页课件](./pages/mvcc-readview.html) · [JSON](./lessons/mvcc-readview.json) · [源码](./src/lab/readview/) |
| 58 | **RR 级快照读防住了幻读，一 UPDATE 怎么又冒出新行？** | 面试常问「MySQL RR 到底有没有彻底解决幻读？」答案是：没有！一旦混用快照读和当前读，幻影行立刻现身。 | `lab.phantom` | [网页课件](./pages/tx-phantom-read.html) · [JSON](./lessons/tx-phantom-read.json) · [源码](./src/lab/phantom/) |
| 59 | **一个长事务挂着不提交，Undo 日志怎么把磁盘撑爆？** | 线上突然告警 ibdata1 / undo 表空间暴涨几十 GB、所有查询变慢——罪魁祸首往往只是一个忘了 commit 的长事务！ | `lab.longtrx` | [网页课件](./pages/mvcc-purge-longtx.html) · [JSON](./lessons/mvcc-purge-longtx.json) · [源码](./src/lab/longtrx/) |

### 模块四 · 第五章：日志两阶段提交、行锁/间隙锁死锁与乐观锁（第 60～65 课）

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 60 | **写内存不立刻刷盘，断电怎么不丢数据？WAL Redo 与 Undo 职责拆解** | 每次改一行就随机刷 16KB 数据页慢如蜗牛；先写顺序 Redo 呢？那为什么还需要 Undo 日志？ | `lab.walredo` | [网页课件](./pages/wal-redo-undo.html) · [JSON](./lessons/wal-redo-undo.json) · [源码](./src/lab/walredo/) |
| 61 | **Redo 和 Binlog 拆开写，主从数据怎么就劈叉了？两阶段提交真相** | InnoDB 有 Redo Log，Server 层有 Binlog。如果先写完一个就断电，主库和从库立刻出现幽灵数据或丢数据！ | `lab.twopc` | [网页课件](./pages/two-phase-commit.html) · [JSON](./lessons/two-phase-commit.json) · [源码](./src/lab/twopc/) |
| 62 | **明明只改一行，怎么把整张表都锁死了？行锁升级表锁现场** | 很多人以为 InnoDB 的行锁就是「按行号加锁」，其实它是「加在索引记录上的」！一旦 WHERE 没走索引，整张表直接锁死。 | `lab.rowlock` | [网页课件](./pages/row-lock-index.html) · [JSON](./lessons/row-lock-index.json) · [源码](./src/lab/rowlock/) |
| 63 | **表里根本没有 id=7，为什么插入 id=7 却被别人卡死？间隙锁与临键锁** | 别人只是用 FOR UPDATE 查了一个根本不存在的 id=7，为什么你去 INSERT id=7 甚至 id=8 全被阻塞了？ | `lab.gaplock` | [网页课件](./pages/gap-nextkey-lock.html) · [JSON](./lessons/gap-nextkey-lock.json) · [源码](./src/lab/gaplock/) |
| 64 | **两事务同时「先查不存在再插入」，怎么双双死锁报错？Gap 锁 + 插入意向锁** | 生产最常见的神秘死锁 Deadlock found：两个并发请求都先 SELECT ... FOR UPDATE 查无此单，再各自 INSERT，直接双双撞死！ | `lab.sqldeadlock` | [网页课件](./pages/deadlock-insert-gap.html) · [JSON](./lessons/deadlock-insert-gap.json) · [源码](./src/lab/sqldeadlock/) |
| 65 | **高并发扣库存：FOR UPDATE 排长队 vs CAS 版本号乐观锁怎么选？** | 秒杀扣库存有四条路：不加锁直接扣、悲观 FOR UPDATE、版本号乐观锁、单条 SQL 条件原子扣减。高并发下谁超卖、谁最稳？ | `lab.optlock` | [网页课件](./pages/optimistic-vs-pessimistic.html) · [JSON](./lessons/optimistic-vs-pessimistic.json) · [源码](./src/lab/optlock/) |

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

### 模块五 · 第三章：Spring MVC 请求链路、参数校验与并发隔离（第 48～52 课）

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 48 | **Filter vs Interceptor：为什么跨域/静态资源在拦截器里漏掉了？** | 同样是「拦截请求」：Filter 在 Servlet 容器外层，Interceptor 在 DispatcherServlet 内层。放错位置，CORS 预检或静态资源全漏掉！ | `lab.mvcchain` | [网页课件](./pages/mvc-filter-interceptor.html) · [JSON](./lessons/mvc-filter-interceptor.json) · [源码](./src/lab/mvcchain/) |
| 49 | **参数校验 @Valid 没生效，或 BindingResult 吞掉了全局异常？** | DTO 上贴满了 @NotNull/@Min，Controller 却忘了加 @Valid；或者顺手多写了个 BindingResult，结果全局异常处理器再也收不到报错！ | `lab.paramvalid` | [网页课件](./pages/mvc-param-bind.html) · [JSON](./lessons/mvc-param-bind.json) · [源码](./src/lab/paramvalid/) |
| 50 | **前后端传 Long 主键与金额：为什么前端收到 ID 尾数全变 0 了？** | 后端雪花算法生成 19 位 Long 主键，传到前端 JSON.parse 后最后三位全变成 0；用 double 算金额，0.1 + 0.2 竟不等于 0.3！ | `lab.jsontrap` | [网页课件](./pages/mvc-jackson-trap.html) · [JSON](./lessons/mvc-jackson-trap.json) · [源码](./src/lab/jsontrap/) |
| 51 | **全局异常 @RestControllerAdvice 为什么兜不住 Filter 抛出的异常？** | 配了 @RestControllerAdvice 统一返回 JSON 错误码，可 JWT Filter 一抛过期异常，前端收到的竟还是 Tomcat 默认 500 网页！ | `lab.exchandler` | [网页课件](./pages/mvc-global-exception.html) · [JSON](./lessons/mvc-global-exception.json) · [源码](./src/lab/exchandler/) |
| 52 | **Tomcat 线程全被慢接口堵死，连健康检查都挂了？** | 一个导出报表慢接口拖垮了整个 Spring Boot 应用：所有核心下单接口和 /actuator/health 全部超时雪崩！ | `lab.mvcthread` | [网页课件](./pages/mvc-thread-pool.html) · [JSON](./lessons/mvc-thread-pool.json) · [源码](./src/lab/mvcthread/) |

### 模块五 · 第四章：MyBatis 持久层与 HikariCP 连接池（第 53～57 课）

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| 53 | **MyBatis 里 #{id} 和 ${id} 只差一个符号，为什么上线就遭 SQL 注入？** | 为了图省事在模糊查询和排序里写了 ${kw}，结果渗透测试一扫直接脱库，还把数据库执行计划缓存打爆！ | `lab.sqlinject` | [网页课件](./pages/mybatis-dollar-hash.html) · [JSON](./lessons/mybatis-dollar-hash.json) · [源码](./src/lab/sqlinject/) |
| 54 | **同一个事务里查了两次余额，怎么第二次读到的还是旧值？** | 账务线程明明已经把余额从 1000 扣成了 800，结果上游方法在同一个 @Transactional 里再查一次，还是 1000！ | `lab.l1cache` | [网页课件](./pages/mybatis-l1-cache.html) · [JSON](./lessons/mybatis-l1-cache.json) · [源码](./src/lab/l1cache/) |
| 55 | **开了 MyBatis 二级缓存，怎么联表查询查出半年前的脏数据？** | 用户早就把昵称从 Alice 改成了 Bob，结果 OrderMapper 联表查订单时，显示的用户名永远是 Alice！ | `lab.l2cache` | [网页课件](./pages/mybatis-l2-cache.html) · [JSON](./lessons/mybatis-l2-cache.json) · [源码](./src/lab/l2cache/) |
| 56 | **数据库连接池不是越大越好？为什么设到 200 反而比设 9 更慢？** | 为了应对高并发把 HikariCP 的 maximum-pool-size 从 10 调到 200，结果接口响应时间直接翻了 3 倍！ | `lab.hikarisize` | [网页课件](./pages/hikari-pool-size.html) · [JSON](./lessons/hikari-pool-size.json) · [源码](./src/lab/hikarisize/) |
| 57 | **服务跑了半小时突然全部 Connection is not available，怎么排查？** | QPS 明明很低，但只要跑够 10 次某个接口，整个系统所有数据库请求就全部超时卡死！ | `lab.hikarileak` | [网页课件](./pages/hikari-leak-trap.html) · [JSON](./lessons/hikari-leak-trap.json) · [源码](./src/lab/hikarileak/) |






### 模块 6：Redis 缓存、MQ 与微服务基础（#M6-01～#M6-24）

纯 JDK 零依赖仿真实验室：覆盖缓存高并发灾难与底层原理、分布式锁与看门狗、MQ 可靠幂等积压、微服务注册/重试/限流熔断。

| # | 课程标题 | 一句话引子 | 代码包 | 入口 |
|---|---|---|---|---|
| M6-01 | **不存在的 ID 疯狂查库：缓存穿透怎么挡？** | 恶意或异常请求专查根本不存在的 ID，缓存与数据库双双未命中，每次都直穿到底层 DB。 | `lab.m6.penetration` | [网页课件](./pages/m6-cache-penetration.html) · [JSON](./lessons/m6-cache-penetration.json) · [源码](./src/lab/m6/penetration/) |
| M6-02 | **热点 Key 过期瞬间：互斥锁 vs 逻辑过期** | 某个爆款热点 Key 到点过期的一瞬间，成百上千并发请求同时 miss 挤向数据库，到底该排队等锁还是直接返回旧值？ | `lab.m6.breakdown` | [网页课件](./pages/m6-cache-breakdown.html) · [JSON](./lessons/m6-cache-breakdown.json) · [源码](./src/lab/m6/breakdown/) |
| M6-03 | **整点批量过期：为什么缓存雪崩打垮库？** | 初始化缓存时给 1 万个 Key 统一设了 `TTL = 3600s`，一小时后整点瞬间全员失效，数据库峰值 QPS 瞬间翻倍宕机。 | `lab.m6.avalanche` | [网页课件](./pages/m6-cache-avalanche.html) · [JSON](./lessons/m6-cache-avalanche.json) · [源码](./src/lab/m6/avalanche/) |
| M6-04 | **先删缓存还是先改库？并发读写脏数据现场** | 更新数据时到底「先删缓存再改库」还是「先改库再删缓存」？用并发实验亲眼看看旧值是怎么被读线程倒灌回缓存的。 | `lab.m6.consistency` | [网页课件](./pages/m6-cache-consistency.html) · [JSON](./lessons/m6-cache-consistency.json) · [源码](./src/lab/m6/consistency/) |
| M6-05 | **顶流热点 Key 打爆单分片：本地二级缓存与 Key 拆分** | 就算 Redis 搭了 16 个分片集群，某一个顶流热搜 Key 还是只会落到其中 1 个分片上，把单核单分片打到 CPU 100%。 | `lab.m6.hotkey` | [网页课件](./pages/m6-hotkey-split.html) · [JSON](./lessons/m6-hotkey-split.json) · [源码](./src/lab/m6/hotkey/) |
| M6-06 | **BigKey 阻塞单线程 + Pipeline 批量往返代价** | 一次查 40 条数据，用 `for` 循环调 40 次 `get` 和用 Pipeline 差多少？大 Key 又是怎么把 Redis 单线程卡死的？ | `lab.m6.bigkey` | [网页课件](./pages/m6-bigkey-pipeline.html) · [JSON](./lessons/m6-bigkey-pipeline.json) · [源码](./src/lab/m6/bigkey/) |
| M6-07 | **Hash 存 100 字段还是 1000 字段？底层编码跳变代价** | 把用户属性拆成独立 String Key 还是塞进 Hash？当 Hash 字段数超过 512 或单值超 64B，底层从 listpack 跳变成 hashtable 内存直接翻几倍。 | `lab.m6.encoding` | [网页课件](./pages/m6-redis-encoding.html) · [JSON](./lessons/m6-redis-encoding.json) · [源码](./src/lab/m6/encoding/) |
| M6-08 | **排行榜为什么用跳表不用红黑树？范围查询实测** | Redis ZSet 底层选了跳表（SkipList）而不是红黑树或 B+ 树。对比线性扫描、红黑树与跳表在范围查询上的表现，看清它为什么又快又好写。 | `lab.m6.skiplist` | [网页课件](./pages/m6-zset-skiplist.html) · [JSON](./lessons/m6-zset-skiplist.json) · [源码](./src/lab/m6/skiplist/) |
| M6-09 | **fork 生成 RDB 快照时，写放大与阻塞怎么来的？** | Redis 执行 `BGSAVE` 会 `fork` 出子进程生成 RDB。虽然号称异步不阻塞，但主进程 `fork` 瞬间和写入触发的 Copy-on-Write（写时复制）依然可能造成内存翻倍与延迟毛刺。 | `lab.m6.rdbcow` | [网页课件](./pages/m6-rdb-cow.html) · [JSON](./lessons/m6-rdb-cow.json) · [源码](./src/lab/m6/rdbcow/) |
| M6-10 | **AOF 三种刷盘策略：always、everysec、no 的吞吐与丢数账** | Redis AOF 日志写进内核 PageCache 并不等于落盘。对比 `no`、`always`、`everysec` 与组提交在崩溃丢数与吞吐上的真实代价。 | `lab.m6.aoffsync` | [网页课件](./pages/m6-aof-fsync.html) · [JSON](./lessons/m6-aof-fsync.json) · [源码](./src/lab/m6/aoffsync/) |
| M6-11 | **内存满时随机淘汰 vs 近似 LRU vs LFU 命中率对比** | Redis 内存触顶 `maxmemory` 时，选 `noeviction` 直接报错、`random` 随机乱删、`allkeys-lru` 还是 `allkeys-lfu`？用真实二八热点负载跑出命中率差距。 | `lab.m6.eviction` | [网页课件](./pages/m6-eviction-lfu.html) · [JSON](./lessons/m6-eviction-lfu.json) · [源码](./src/lab/m6/eviction/) |
| M6-12 | **SETNX 和 EXPIRE 分两行写，崩溃怎么变成永久死锁？** | 分布式锁最基础的三个坑：两步加锁崩溃变永久死锁、无唯一 Token 误删别人锁、忘记释放锁。 | `lab.m6.distlock` | [网页课件](./pages/m6-dist-lock-nx.html) · [JSON](./lessons/m6-dist-lock-nx.json) · [源码](./src/lab/m6/distlock/) |
| M6-13 | **get 判断锁归属再 del，为什么还是删了别人的锁？** | 就算在 Java 代码里先 `get` 校验 UUID 再 `del`，两步之间的一次 GC 停顿就足够让锁过期易主，照样删掉别人的锁！看懂 Lua 脚本与 Fencing Token 的必要性。 | `lab.m6.luaunlock` | [网页课件](./pages/m6-lua-unlock.html) · [JSON](./lessons/m6-lua-unlock.json) · [源码](./src/lab/m6/luaunlock/) |
| M6-14 | **业务跑超时锁提前过期，看门狗怎么自动续期？** | 锁 TTL 设 3 秒，结果业务跑了 5 秒，另一个线程立刻闯进来双写撞车；设 5 分钟又怕机器挂了卡死。看懂 Redisson Watchdog 看门狗的续期与停续逻辑。 | `lab.m6.watchdog` | [网页课件](./pages/m6-watchdog-renew.html) · [JSON](./lessons/m6-watchdog-renew.json) · [源码](./src/lab/m6/watchdog/) |
| M6-15 | **主从异步复制丢锁 vs 分段分布式锁吞吐对比** | Redis 主从异步复制时，刚加完锁 Master 就宕机，Slave 升主后锁直接蒸发！此外，一把全局大锁怎么拆成分段锁把吞吐翻倍？ | `lab.m6.redlock` | [网页课件](./pages/m6-lock-granularity.html) · [JSON](./lessons/m6-lock-granularity.json) · [源码](./src/lab/m6/redlock/) |
| M6-16 | **发 MQ 不等确认、消费者自动 ACK，消息是怎么悄悄丢的？** | 消息队列丢消息只可能发生在三段路：生产者发丢、Broker 存丢、消费者还没处理完就自动 ACK 丢。把三端确认和本地消息表接起来才能做到零丢失。 | `lab.m6.mqconfirm` | [网页课件](./pages/m6-mq-reliability.html) · [JSON](./lessons/m6-mq-reliability.json) · [源码](./src/lab/m6/mqconfirm/) |
| M6-17 | **MQ 重试发了两次消息，为什么用户被扣了两次钱？** | 第 16 课的「至少一次投递」保证了消息不丢，但也带来了重复消息。只靠内存/Redis 判断 `if (exists) return` 依然会有并发竞态，真正稳的是数据库唯一索引和状态机 CAS。 | `lab.m6.mqidem` | [网页课件](./pages/m6-mq-idempotent.html) · [JSON](./lessons/m6-mq-idempotent.json) · [源码](./src/lab/m6/mqidem/) |
| M6-18 | **订单创建、支付、发货三条消息并发消费，怎么乱序了？** | MQ 默认把消息轮询分散到多个分区并由多线程并发消费，同一笔订单的「创建→支付→发货」落到不同分区或不同线程就会彻底乱序。按业务 Key 哈希路由 + 分区内串行（或局部有序缓冲）才能既保顺序又保吞吐。 | `lab.m6.mqorder` | [网页课件](./pages/m6-mq-order.html) · [JSON](./lessons/m6-mq-order.json) · [源码](./src/lab/m6/mqorder/) |
| M6-19 | **30 分钟未支付自动关单：定时扫表、时间轮还是延迟队列+死信？** | 定时扫数据库全表会随订单膨胀把 DB 扫崩；为每个订单开一个睡眠线程会瞬间耗尽线程池。延迟消息底层靠时间轮/分层队列把扫描次数降到 O(1)，配合死信队列还能兜底异常消息。 | `lab.m6.mqdelay` | [网页课件](./pages/m6-mq-delay-dlq.html) · [JSON](./lessons/m6-mq-delay-dlq.json) · [源码](./src/lab/m6/mqdelay/) |
| M6-20 | **百万消息积压在 MQ 里，为什么加机器反而可能把消费者打 OOM？** | 大促突发流量或下游故障恢复后，MQ 积压几十万条消息。盲目用无界线程池/无界队列狂拉消息会瞬间撑爆消费者内存 OOM；正确姿势是「扩容分区 + 临时 Topic 分片转移 + 有界批量拉取反压」。 | `lab.m6.mqbacklog` | [网页课件](./pages/m6-mq-backlog.html) · [JSON](./lessons/m6-mq-backlog.json) · [源码](./src/lab/m6/mqbacklog/) |
| M6-21 | **注册中心心跳全超时，为什么 Eureka 死保旧列表而 Zookeeper 直接踢光？** | 微服务注册中心不是简单的心跳打卡表：网络抖动时如果不管三七二十一全踢掉，健康节点也会团灭；如果连本地快照都不缓存，注册中心一挂全网跟着瘫痪。 | `lab.m6.registry` | [网页课件](./pages/m6-registry-heartbeat.html) · [JSON](./lessons/m6-registry-heartbeat.json) · [源码](./src/lab/m6/registry/) |
| M6-22 | **下游已经快被压垮了，为什么盲目重试 3 次会直接引发雪崩？** | RPC 超时重试是一把双刃剑：偶尔网络抖动时重试能救活请求；但如果下游本就过载，所有上游同时盲目同步重试 3 次，流量瞬间放大 3 倍把下游彻底打死！必须配合退避抖动、重试预算与接口幂等。 | `lab.m6.rpcretry` | [网页课件](./pages/m6-rpc-retry.html) · [JSON](./lessons/m6-rpc-retry.json) · [源码](./src/lab/m6/rpcretry/) |
| M6-23 | **固定窗口限流明明设了 10 QPS，为什么边界一秒放过了 20 个请求？** | 限流算法不是随便计数就行：固定窗口在时间临界点会被「双倍突刺」打穿；滑动窗口精准统计最近区间却占内存；令牌桶（Guava RateLimiter）既能恒定速率放行又允许可控突发。 | `lab.m6.ratelimit` | [网页课件](./pages/m6-rate-limiter.html) · [JSON](./lessons/m6-rate-limiter.json) · [源码](./src/lab/m6/ratelimit/) |
| M6-24 | **服务 A 慢查询卡死，为什么健康的服务 B 跟着全线崩溃？** | 微服务最怕「级联雪崩」：所有接口共用同一个 Tomcat 线程池时，慢服务 A 会把线程全占光，导致健康的服务 B 也拿不到线程！必须靠舱壁隔离（Bulkhead）、超时快速失败与熔断器状态机守住防线。 | `lab.m6.breaker` | [网页课件](./pages/m6-circuit-breaker.html) · [JSON](./lessons/m6-circuit-breaker.json) · [源码](./src/lab/m6/breaker/) |

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
