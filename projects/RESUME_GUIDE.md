# 模块 7：简历 STAR 量化模板与面试连环追问（含二面深挖防御）

> **铁律提醒**：写进简历的每一句话，面试官都会顺着往下刨三层！一定要先把 [`FoundationPitfallDrillsTest`](./project-a-admin/src/test/java/com/javahood/admin/drills/FoundationPitfallDrillsTest.java) 和 [`SeckillFoundationDrillsTest`](./project-b-seckill/src/test/java/com/javahood/seckill/drills/SeckillFoundationDrillsTest.java) 里的 10 个对照测试跑熟，再把下面的要点改写成你自己的语言写进简历。

---

## 一、简历项目描述模板（STAR 量化版）

### 项目 B：高并发秒杀抢券与订单履约系统（核心亮点项目）
- **项目背景（S/T）**：针对大促秒杀场景下热点读击穿、单点库存锁竞争、异步削峰丢单及超时未付库存泄漏等痛点，基于 **Spring Boot 3 + Redis + MySQL** 设计并实现高并发抢券与履约系统。
- **核心工作（A）**：
  1. **多级读保护与分片原子预扣**：详情页采用 **SingleFlight（`CompletableFuture` 请求合并）+ 空值哨兵 + 随机 TTL** 防护缓存击穿、穿透与雪崩；写链路将活动库存拆分为 **16 个分片桶**，结合 **Redis Lua 脚本 / 分片 CAS + 顺延探测** 在单次原子操作内完成「按人限领去重 + 库存预扣」，将单点热点竞争降低 16 倍。
  2. **有界反压削峰与全链路追踪**：异步落库采用 **有界 `ArrayBlockingQueue` 线程池** 并封装 `MDC` 快照装饰器实现跨线程 `traceId` 透传与清理；队列饱和时触发显式拒绝并立即回滚预扣库存（返回 `429`），杜绝无界队列 OOM 与静默少卖。
  3. **发件箱最终一致与状态机闭环**：订单与 **`tx-outbox` 本地消息表** 同库同事务原子落盘，由中继任务投递下游并基于 **`uk_coupon_order_no` 唯一约束实现幂等发券**；支付回调与超时关单采用 **SQL CAS 条件更新** 互斥流转，仅在 `casWon == 1` 时回补 DB 库存并通过 **`TransactionSynchronizationManager.afterCommit()`** 同步归还分片库存，彻底根治并发重复回补引发的超卖。
- **量化结果（R）**：单机基准实测对比同步数据库锁方案（`~430 req/s`），分片预扣 + 异步发件箱架构峰值吞吐提升至 **`50,000+ req/s`**；40 并发热点冷读合并为 **1 次** DB 回源；32 线程 1600 次并发压测实现 **0 超卖、0 重复领券、100% 幂等发券一致**。

---

### 项目 A：企业级 RBAC 后台管理系统（工程规范底座）
- **项目背景（S/T）**：构建符合生产安全与性能规范的企业后台底座，解决无状态 JWT 无法主动吊销、JPA 分页 N+1 查询、大数据导出 OOM 及业务回滚导致失败审计丢失等问题。
- **核心工作（A）**：
  1. **认证安全与防爆破**：实现 **JWT 双 Token 一次性轮换机制**（强校验 `type` 防混淆攻击，Refresh 原子消费防重放，Logout 基于剩余 TTL 写黑名单），前置 `ConcurrentHashMap.compute` 原子登录防爆破拦截；构建用户-角色-权限三级 **RBAC** 鉴权。
  2. **持久层调优与安全流式导出**：关闭 OSIV（`open-in-view: false`），采用 **`FetchType.LAZY` + `@BatchSize` + `@EntityGraph`** 消除分页 N+1 查询；基于分批流式写入导出 **UTF-8 BOM CSV**，并对 `= + - @` 前缀字段做公式注入转义。
  3. **独立事务审计与容器化交付**：基于 **Spring AOP + 独立 Bean `Propagation.REQUIRES_NEW`** 记录含 `traceId` 的操作审计日志，确保业务异常回滚时失败现场仍可靠落库；编写多阶段非 root `Dockerfile`、`docker-compose` 健康检查编排与 Nginx 反代配置。

---

## 二、一二面高频连环追问（Top 12 满分答法）

1. **问：为什么把库存拆成 16 个分片？如果我的哈希分片桶卖完了，别的桶还有货，用户不就误判售罄了吗？**
   - **答**：单 Key 或单原子类在高并发下所有请求串行排队/CAS 自旋冲突极高，拆 16 桶能把竞争分散 16 倍。当 `Math.floorMod(userId.hashCode(), 16)` 命中的起始桶余量为 0 时，代码会顺着 `(start + offset) % 16` 线性顺延探测其余 15 个桶，只有 16 个桶全为 0 才返回 `SOLD_OUT`，兼顾高吞吐与 100% 库存利用率。

2. **问：秒杀异步线程池队列满了你怎么办？为什么不用 `DiscardPolicy` 或 `CallerRunsPolicy`？**
   - **答**：`DiscardPolicy` 静默扔任务会导致预扣库存已减 1 但订单没落库，造成永久少卖；`CallerRunsPolicy` 会让 Tomcat HTTP 线程自己去跑慢 SQL，瞬间把整个 Web 容器线程池堵死。我采用有界 `ArrayBlockingQueue` + 抛 `RejectedExecutionException`，在 `catch` 块立刻调 `preDeductEngine.rollback()` 把预扣库存和用户限购名额原路退回，并返回 HTTP `429` 提示稍后重试，保证账实严格一致。

3. **问：用户抢到待支付订单但 15 分钟没付款，超时关单回补库存怎么防并发多补？**
   - **答**：这是非常容易出 P0 事故的点。首先执行 `UPDATE seckill_order SET status='CLOSED' WHERE order_no=? AND status='PENDING'`，利用 InnoDB 行锁保证哪怕 16 个线程并发关单，也**只有 1 个线程拿到返回值 `1`**。只有 `casWon == 1` 的线程才执行 `remaining_stock + 1`（且带 `remaining_stock < total_stock` 保护）；同时用 `TransactionSynchronizationManager.registerSynchronization` 在**数据库事务真正 `afterCommit` 之后**才往缓存分片池加回 1 个库存，防止 DB 事务未提交或回滚导致缓存与数据库库存倒挂。

4. **问：为什么要搞 `tx_outbox` 本地消息表？有了它下游发券还需要做幂等吗？**
   - **答**：本地写库和发远程 MQ 无法放进同一个本地事务，先写库后发 MQ 可能因网络抖动丢消息，或在事务提交前把连接池拖死。同库同事务写 `seckill_order` 和 `tx_outbox` 利用了 InnoDB 本地 ACID 保证事件必不丢。但中继投递本质是 **At-Least-Once（至少一次）**，ACK 超时一定会重发，因此下游 `CouponFulfillmentConsumer` 必须配合 `user_coupon` 表的 `uk_coupon_order_no` 唯一索引做幂等兜底，重投多少次都只发 1 张券。

5. **问：活动详情页冷启动或刚好过期时几万人同时刷，你怎么防把 MySQL 打死？**
   - **答**：我在 `CampaignQueryService` 做了三层保护：① **SingleFlight 请求合并**（`ConcurrentHashMap<Long, CompletableFuture>`），同一毫秒 40 个线程冷读同一个活动，只有 `putIfAbsent` 成功的 1 个线程查 DB，其余 39 个 `join()` 共享结果；② **短 TTL 空值哨兵**，防恶意刷不存在的负数 ID 穿透打库；③ **随机 TTL 抖动**，防批量活动同一秒集中过期雪崩。

6. **问：异步丢进线程池之后，日志里的 `traceId` 怎么不丢、而且不串给下一单？**
   - **答**：`MDC` 底层是普通 `ThreadLocal`，池化工作线程是提前建好复用的。我在自定义线程池重写了 `execute(Runnable)`：在主线程提交任务瞬间调用 `MDC.getCopyOfContextMap()` 拍下不可变快照，工作线程执行前 `setContextMap` 恢复，并在 `finally` 里强制 `clear()` 还原现场。

7. **问：为什么你的 `@Audited` 审计切面要单独调另一个类的 `@Transactional(propagation = Propagation.REQUIRES_NEW)`？**
   - **答**：两个原因。第一，如果被审计的业务方法本身有 `@Transactional` 且抛异常回滚，默认的 `REQUIRED` 会让审计日志共用同一个已标为 rollback-only 的连接，导致报错现场的审计日志跟着业务数据一起蒸发；`REQUIRES_NEW` 会挂起外层脏事务，新借一条连接独立提交失败日志。第二，Spring 事务靠动态代理生效，必须拆到独立的 `AuditLogPersistenceService` Bean 调用，避免同类 `this` 自调用绕过代理。

8. **问：为什么把 `spring.jpa.open-in-view` 关了，又把 `@ManyToMany` 的 `EAGER` 改成 `LAZY + @BatchSize`？**
   - **答**：`open-in-view: true` 会让数据库连接在离开 `@Transactional` 后依然被占着直到 HTTP 响应写完，高并发极易耗尽 HikariCP；而 `EAGER` 在分页查 20 个用户时会触发 20 条额外关联 SQL（N+1 问题）。关掉 OSIV 后，在只读事务内用 `LAZY + @BatchSize(50)` 把关联查询合并成 1 条 `WHERE user_id IN (...)` 批量 SQL，并当场映射成不可变 `UserVO` Record 返回，既保住 SQL `LIMIT` 分页，又彻底消灭 N+1 和懒加载异常。

9. **问：Access Token 和 Refresh Token 用同一把密钥签名，黑客拿 7 天的 Refresh Token 当 Access Token 调接口能成功吗？**
   - **答**：不能。我在签发时显式写入了 `type` claim（`ACCESS` / `REFRESH`），`JwtAuthFilter` 只接受 `parseAndValidate(token, TokenType.ACCESS)`，拿 Refresh Token 冒充会在 `type` 校验时直接拦截。

10. **问：Refresh Token 为什么设计成用一次就删（Rotation）？**
    - **答**：如果 Refresh Token 在 7 天内可无限次重复使用，一旦泄露攻击者就能悄无声息长期续期；改成原子消费（Redis `GETDEL` / 内存 `remove`）后，旧 Token 一旦被二次重放立刻返回 `40102` 告警。

11. **问：后台导出 CSV 怎么同时防 OOM、防中文乱码、防安全漏洞？**
    - **答**：按每页 200 条切片查询流式写入 `Response.getWriter()`，堆内存恒定只占 200 条；开头写入 `\uFEFF` UTF-8 BOM 让 Windows Excel 正确识别编码；对 `= + - @` 开头单元格前置单引号 `'` 并转义双引号，阻断 CSV 公式注入执行外部命令。

12. **问：登录接口如果有人拿字典撞库狂刷密码，BCrypt 哈希很吃 CPU，怎么防？**
    - **答**：在进 `AuthenticationManager` 做昂贵的 BCrypt 校验之前，先过 `LoginAttemptGuard`，用 `ConcurrentHashMap.compute()` 按账号原子统计窗口内连续失败次数，满 5 次直接短路抛 `42901`，把撞库流量在微秒级内存判断中直接挡掉。
