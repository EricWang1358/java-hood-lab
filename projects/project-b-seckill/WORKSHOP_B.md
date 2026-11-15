# 项目 B 逐节深潜手册（第 11～20 节：高并发秒杀抢券与订单履约系统）

> 阅读本手册前，建议先读完 [`../FOUNDATIONS_MAP.md`](../FOUNDATIONS_MAP.md) 第四节，把「五道防线流水线」印在脑海里。
> 每一节都直接串联你在前序模块亲手跑过的并发/数据库/Redis 实验课，做到**「原理有出处、代码有实测、破坏有报错」**。

---

## 第 11 节：秒杀数据模型与数据库「最后一道铁闸」

- **【底层地基】（接模块 0 `#02 stock-oversell` & 模块 4 行锁）**：
  - 无论前面的缓存、分片、队列玩得多花，关系型数据库永远是资产安全的最后一道底线：
    1. **防超卖底线**：绝不在 Java 里 `select stock -> if (stock > 0) -> update stock = stock - 1`！必须用单条原子 SQL：`UPDATE coupon_campaign SET remaining_stock = remaining_stock - 1 WHERE id = :id AND remaining_stock > 0`。在 MySQL InnoDB 下，这条 `UPDATE` 会对主键 `id` 加排他行锁（X 锁），重读最新值并校验 `> 0`，影响行数返回 `0` 说明已空。
    2. **防一人多领底线**：订单表必须建联合唯一索引 `UNIQUE KEY uk_campaign_user (campaign_id, user_id)`，就算上游并发漏放了同一个人的两个请求进来，第二个 `INSERT` 也会被唯一键当场弹回。
- **【本仓源码深潜】**：
  - [`db/schema.sql`](./db/schema.sql)、[`CouponCampaignRepository.java`](./src/main/java/com/javahood/seckill/repository/CouponCampaignRepository.java)、[`SeckillOrder.java`](./src/main/java/com/javahood/seckill/domain/SeckillOrder.java)。
- **【开口自检】**：既然这一条 SQL 就能保证绝对不超卖，为什么大厂秒杀不直接让 10 万并发全打这条 `UPDATE` SQL？（提示：10 万个线程排队抢 InnoDB 同一行记录的 X 锁，HikariCP 连接池和数据库会发生什么？）

---

## 第 12 节：热点读防护 —— SingleFlight 防击穿、空哨兵防穿透、随机 TTL 防雪崩

- **【底层地基】（接模块 0 `#25 cache-aside` & 模块 6 缓存三大灾难）**：
  - 秒杀开抢前 10 秒，几万人疯狂刷新 `GET /api/seckill/campaigns/{id}` 看倒计时和余量，「读流量」往往是「写流量」的几十倍！
  - 三大缓存灾难必须在读链路一次防齐：
    - **击穿（Breakdown）**：热点活动缓存刚过期的 1 毫秒内涌入 500 个请求。用 `ConcurrentHashMap<Long, CompletableFuture<...>> inFlight` 做 **SingleFlight 请求合并**——只有第一个 `putIfAbsent` 成功的线程去查库，其余 499 个线程全部 `existing.join()` 坐享其成！
    - **穿透（Penetration）**：黑客狂刷 `campaignId = -999999`，数据库没有，缓存永远不中。查库为空时也往缓存塞一个 3 秒短寿命的「空值哨兵（Null Sentinel）」，把穿透请求挡在内存里。
    - **雪崩（Avalanche）**：批量预热 100 个活动如果全设死 10 秒过期，第 10 秒集体失效。给过期时间加上 `ThreadLocalRandom` 随机抖动（Jitter）。
- **【本仓源码深潜】**：
  - [`CampaignQueryService.java`](./src/main/java/com/javahood/seckill/service/CampaignQueryService.java)。
- **【亲手破坏与断点任务】**：
  - 打开 [`SeckillFoundationDrillsTest.java`](./src/test/java/com/javahood/seckill/drills/SeckillFoundationDrillsTest.java) 运行 `drill04_hotspotCampaignQuery_singleFlightAndNullSentinelCoalesceToSingleDbHit`：40 个并发冷读只触发 **1 次** DB 查询，40 个并发刷不存在 ID 也只触发 **1 次** DB 查询！
- **【开口自检】**：`inFlight.remove(campaignId, ownFuture)` 为什么要放在 `finally` 里？如果不小心漏写了 `finally`，万一那一次查库超时抛了异常，后面所有请求会怎样？

---

## 第 13 节：16 分片 CAS 库存桶 + 顺延探测 + 按人限领拦截

- **【底层地基】（接模块 0 `#14 adder-limit`、`#23 once-per-user`、`#26 sharded-stock`）**：
  - 为什么不能用 `LongAdder` 卡抢券上限？因为 `LongAdder.sum()` 不是原子快照，边加边求和必超发（第 14 课）！
  - 为什么单 `AtomicInteger` 比不过 16 分片？因为单变量在高并发下所有 CPU 核心都在抢同一块缓存行做 CAS 自旋；拆成 `AtomicIntegerArray(16)` 后，买家按 `Math.floorMod(userId.hashCode(), 16)` 散列到各自的分片桶做 CAS 扣减，热点冲突瞬间降低 16 倍！
  - 当自己命中的第 `k` 号桶刚好卖空（`0`）但其他桶还有余票时，顺着 `(start + offset) % 16` **线性顺延探测**一圈，直到全部 16 个桶都为 `0` 才判定真正售罄。
  - **细节坑**：`Math.abs(Integer.MIN_VALUE)` 在 Java 里依然是负数（溢出）！所以散列取模一定要用 **`Math.floorMod(hash, shardCount)`**。
- **【本仓源码深潜】**：
  - [`LocalShardedPreDeductEngine.java`](./src/main/java/com/javahood/seckill/engine/LocalShardedPreDeductEngine.java)。
- **【亲手破坏与断点任务】**：
  - 在 `LocalShardedPreDeductEngine.tryPreDeduct` 第 61 行看到 `users.remove(userId);` 了吗？思考一下：当一个新买家抢券时先 `users.add(userId)` 占位，结果扫完 16 个桶发现全卖光了，为什么要把 `userId` 从 `claimedUsers` 里删掉？（提示：如果紧接着第 18 节有未付款订单超时回补了 1 张库存，这位刚刚没抢到的人还能不能再试？）
- **【开口自检】**：画出「100 张库存分到 16 个桶（前 4 桶各 7 张，后 12 桶各 6 张）」与「桶空顺延探测」的过程。

---

## 第 14 节：多实例部署下的 Redis Lua 脚本原子预扣

- **【底层地基】（接模块 6 Redis 单线程原子性与 Lua 脚本）**：
  - 单机内存分片最快，但如果秒杀服务部署了 4 台机器，就必须把预扣状态集中到 Redis。
  - 如果在 Java 代码里分三步调 Redis：`SISMEMBER` 判重 → `GET` 看库存 → `DECR` 扣库存，由于三步之间穿插了网络往返，高并发下两个请求同时看到 `stock == 1` 再同时 `DECR`，立刻超卖成 `-1`！
  - Redis 执行 **Lua 脚本**期间是单线程串行不可打断的：把「查是否领过 → 查当前分片库存 → `DECR` → `SADD` 记用户名单」写进同一段 `seckill_pre_deduct.lua`，一次网络 RTT 原子跑完。
- **【本仓源码深潜】**：
  - [`seckill_pre_deduct.lua`](./src/main/resources/scripts/seckill_pre_deduct.lua) 与 [`RedisLuaPreDeductEngine.java`](./src/main/java/com/javahood/seckill/engine/RedisLuaPreDeductEngine.java)。
- **【开口自检】**：如果生产用的是 Redis Cluster 集群，同一个 Lua 脚本同时操作 `KEYS[1]`（库存）和 `KEYS[2]`（已领集合），怎么保证这两个 Key 落在同一个 Hash Slot 不报 `CROSSSLOT` 错误？（提示：Redis Hash Tag `{campaignId}`！）

---

## 第 15 节：有界异步线程池、显式反压回滚与跨线程 `MDC` 透传

- **【底层地基】（接模块 0 `#19 tl-inherit` & `#20 pool-reject`）**：
  - 预扣成功后，真正的数据库事务写入丢进异步线程池削峰，HTTP 接口立刻返回 `202 Accepted`。这里藏着两个新手必踩的大雷：
    1. **队列与拒绝策略雷**：用 `Executors.newFixedThreadPool`（底层是无界 `LinkedBlockingQueue(Integer.MAX_VALUE)`）会在数据库变慢时堆积百万任务直接 OOM；而改用有界队列如果配 `DiscardPolicy` 静默丢弃，就会出现**「预扣库存已经扣了 1，写库任务却被悄悄扔掉」**，造成少卖且用户查不到单！正确做法：**有界 `ArrayBlockingQueue` + 饱和抛 `RejectedExecutionException` + `catch` 里立刻 `preDeductEngine.rollback(...)` 归还预扣库存与限购资格，向前端返回 `429 Too Many Requests`**！
    2. **异步日志断链雷**：主线程的 `MDC(traceId)` 进线程池就没了。在自定义线程池重写 `execute(Runnable)`：提交瞬间在主线程 `MDC.getCopyOfContextMap()` 拍快照，子线程执行前 `setContextMap`，`finally` 里 `clear()`。
- **【本仓源码深潜】**：
  - [`SeckillExecutorConfig.java`](./src/main/java/com/javahood/seckill/config/SeckillExecutorConfig.java) 与 [`SeckillService.java`](./src/main/java/com/javahood/seckill/service/SeckillService.java) 的 `claimOptimized`。
- **【亲手破坏与断点任务】**：
  - 运行 `SeckillFoundationDrillsTest#drill01`（验证异步 `traceId` 落库与清理）和 `drill05`（验证队列塞满反压后库存一分不少地回滚到 10，排空后重试成功）！
  - 试着把 `SeckillService.claimOptimized` 第 77 行 `catch (RejectedExecutionException)` 里的 `preDeductEngine.rollback(...)` 注释掉，跑一次 `drill05` 看库存当场泄漏成 9！测完记得恢复。
- **【开口自检】**：为什么秒杀异步落库线程池不能用 `CallerRunsPolicy`（让调用方 Tomcat 线程自己去写数据库）？（提示：一旦数据库抖动，200 条 Tomcat HTTP 线程全被拖住自己写库，连活动详情页和已售罄拦截都响应不了了！）

---

## 第 16 节：本地消息表 `tx-outbox` —— 为什么不能「先写库再发 MQ」？

- **【底层地基】（接模块 0 `#32 tx-outbox` & 模块 5 事务代理）**：
  - 订单落库后需要发 MQ 消息通知下游「给用户账户正式发券、加积分」。
  - 如果写成 `@Transactional { save(order); mqProducer.send(msg); }`：
    - 情况 A：`save` 成功，`mqProducer.send` 网络抖动超时抛异常 -> 整个事务回滚，或者 catch 住异常不回滚导致**订单有了但券永远没发**；
    - 情况 B：`mqProducer.send` 耗时 2 秒 -> 数据库连接被白白霸占 2 秒，HikariCP 瞬间枯竭；
    - 情况 C：`mqProducer.send` 发快了，下游消费者收到消息回头查订单表，结果你这边本地事务还没 `commit`，下游查不到单傻眼了！
  - **`tx-outbox` 标准解法**：在**同一个本地数据库事务**里同时执行 3 条 SQL：`扣减 DB 库存` + `INSERT seckill_order` + `INSERT tx_outbox (status='PENDING')`。利用 MySQL InnoDB 本地 ACID 事务保证**「订单在，待发事件就一定在；订单回滚，待发事件也一定不在」**！
- **【本仓源码深潜】**：
  - [`SeckillOrderPersistenceService.java`](./src/main/java/com/javahood/seckill/service/SeckillOrderPersistenceService.java) 与 [`TxOutbox.java`](./src/main/java/com/javahood/seckill/domain/TxOutbox.java)。
- **【开口自检】**：为什么 `persistOrderAndOutbox` 必须单独放在 `SeckillOrderPersistenceService` 里，而不能作为私有方法或同类方法写在 `SeckillService` 里？

---

## 第 17 节：发件箱定时中继 + 下游「唯一键幂等发券」完整闭环

- **【底层地基】（接模块 6 MQ 三端防丢与重复消费幂等）**：
  - `OutboxRelayScheduler` 每 200ms 捞出一批 `PENDING` 事件投递给消息总线，成功后改 `SENT`，异常则累加 `retryCount`（超过 5 次标 `FAILED` 告警人工介入）。
  - **关键闭环**：中继器保证的是 **At-Least-Once（至少投递一次）**。如果投递成功但改 `SENT` 前机器重启，同一条 `ORDER_CREATED` 事件会被投递第 2 次！因此下游 [`CouponFulfillmentConsumer`](./src/main/java/com/javahood/seckill/outbox/CouponFulfillmentConsumer.java) 在往 `user_coupon` 表发券时，既做前置 `existsByOrderNo` 判重，又靠数据库唯一约束 **`uk_coupon_order_no(order_no)`** 兜底并发重投，做到万无一失。
- **【亲手破坏与断点任务】**：
  - 运行 `SeckillFoundationDrillsTest#drill03_atLeastOnceDuplicateDelivery_idempotentConsumerIssuesStrictlyOneCoupon`，单步跟踪同一事件连续消费 5 次的过程。
- **【开口自检】**：如果不用定时任务轮询 `tx_outbox` 表，生产环境还有哪种更低延迟的方式把 `tx_outbox` 的新增行投递进 Kafka/RocketMQ？（提示：MySQL Binlog CDC，如 Canal / Debezium！）

---

## 第 18 节：CAS 订单状态机 + 超时关单安全回补库存（`afterCommit` 钩子）

- **【底层地基】（接模块 0 `#24 order-state`）**：
  - 同一笔待支付订单 `PENDING`，第 15 分钟整的时候「用户刚扫完码的微信支付回调」和「系统超时关单任务」同时扑过来；或者两个超时扫描实例同时扫到这笔单。
  - **两条铁律**：
    1. **状态流转用条件更新 CAS**：`UPDATE seckill_order SET status = :target WHERE order_no = :no AND status = 'PENDING'`。谁返回 `1` 谁赢，返回 `0` 的直接放弃。
    2. **回补库存必须绑定在 `casWon == 1` 且缓存回补延迟到 `afterCommit()`**：只有赢下 `PENDING -> CLOSED` 的那**唯一一个线程**能给数据库 `remaining_stock + 1`（带 `< total_stock` 上限保护），并通过 `TransactionSynchronizationManager.registerSynchronization` 等数据库事务真正提交成功后，再往分片池加回 1 个库存供其他买家争抢！
- **【本仓源码深潜】**：
  - [`OrderStateService.java`](./src/main/java/com/javahood/seckill/service/OrderStateService.java) 第 46～76 行。
- **【亲手破坏与断点任务】**：
  - 运行 `SeckillFoundationDrillsTest#drill02_timeoutCloseStockRestore_16ConcurrentClosersRestoreStrictlyOnce_andNextBuyerSucceeds`！观察 16 个并发关单线程同时扑向 1 笔未付订单时，库存如何精准从 `0` 变回 `1`（而不是变成 `16`），并且下一位买家 `8002` 顺利接盘成功！
- **【开口自检】**：如果在 `closeTimeout` 里不判断 `casWon != 1` 就直接执行 `incrementStock`，会发生什么严重生产事故？为什么要把 `preDeductEngine.restoreStockAfterClose` 放进 `afterCommit()`？

---

## 第 19 节：32 线程并发压测与基准对照实验

- **【底层地基】**：
  - 性能优化绝不能拍脑袋说「我很快」，必须在同一台机器、同一批请求规模下摆出 **Baseline（同步锁直接打库） vs Optimized（分片预扣 + 异步发件箱）** 的硬指标对比。
- **【本仓源码深潜与实测】**：
  - 运行 [`SeckillConcurrencyBenchmarkTest.java`](./src/test/java/com/javahood/seckill/SeckillConcurrencyBenchmarkTest.java) 与 [`SeckillControllerIntegrationTest.java`](./src/test/java/com/javahood/seckill/SeckillControllerIntegrationTest.java)：
    - **正确性对账**：100 张库存面对 32 线程 1600 次并发请求（800 个用户每人连点 2 次），实测 `accepted = 100`，数据库落单 `100` 笔，剩余库存 `0`，`tx_outbox` 待发残留 `0`，下游发券 `100` 张。
    - **吞吐量对比**：同步锁逐笔打库基线约 `~430 req/s`，分片预扣 + 异步落库路径达 **`50,000+ req/s`**（提升超 **100 倍**）。

---

## 第 20 节：JMeter 外部压测、Docker 生产编排与 STAR 简历答辩

- **【动手步骤】**：
  1. 查看 [`Dockerfile`](./Dockerfile)、[`docker-compose.yml`](./docker-compose.yml)、[`application-prod.yml`](./src/main/resources/application-prod.yml)（切换真实 MySQL 8 + Redis 7 Lua 引擎）。
  2. 使用 [`jmeter/seckill-benchmark.jmx`](./jmeter/seckill-benchmark.jmx) 对 `POST /api/seckill/campaigns/1/claim` 发起 200 并发线程压测，观察聚合报告中的 Throughput、Average、99% Line 与 Error%。
  3. 对照 [`../RESUME_GUIDE.md`](../RESUME_GUIDE.md)，把全部 5 道防线闭上眼睛画在白纸上，并用手机录音完成 2 分钟脱稿项目介绍！
