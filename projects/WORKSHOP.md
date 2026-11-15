# 模块 7：20 节实战拆解工坊总路线图

> **如果你觉得看框架代码「心里发虚、基础不牢」，请严格按下面 4 步走（不要跳步！）**：
> 1. **先补地基透视图（30 分钟）**：精读 [`FOUNDATIONS_MAP.md`](./FOUNDATIONS_MAP.md) —— 一次看懂 Tomcat 线程池、`Filter`、`ThreadLocal`/`MDC`、Spring 动态代理洋葱圈、JPA `LAZY+@BatchSize` 与秒杀五道防线到底是怎么咬合的。
> 2. **再开交互工作台边测边学**：双击打开 [`workshop-lab.html`](./workshop-lab.html)（零外链离线可用），逐节做基础热身问答、看正反代码对照、领断点破坏任务。
> 3. **顺着逐节手册跑通 10 个「踩坑对照演练」**：
>    - 上半场（第 01～10 节）：精读 [`project-a-admin/WORKSHOP_A.md`](./project-a-admin/WORKSHOP_A.md)，在 IDE Debug 运行 [`FoundationPitfallDrillsTest.java`](./project-a-admin/src/test/java/com/javahood/admin/drills/FoundationPitfallDrillsTest.java)（5 组实验）。
>    - 下半场（第 11～20 节）：精读 [`project-b-seckill/WORKSHOP_B.md`](./project-b-seckill/WORKSHOP_B.md)，在 IDE Debug 运行 [`SeckillFoundationDrillsTest.java`](./project-b-seckill/src/test/java/com/javahood/seckill/drills/SeckillFoundationDrillsTest.java)（5 组实验）。
> 4. **最后做 2 分钟脱稿检验**：对照 [`RESUME_GUIDE.md`](./RESUME_GUIDE.md) 白纸画架构图 + 开口回答 Top 12 面试连环追问。

---

## 20 节完整课程与演练对照表

### 上半场：项目 A — 企业级 RBAC 后台系统（[`WORKSHOP_A.md`](./project-a-admin/WORKSHOP_A.md)）

| 节次 | 课程主题 | 补牢哪块基础地基 | 对应源码与可运行演练 |
|---|---|---|---|
| **01** | Maven 脚手架、Profile 隔离与关掉 OSIV | 配置优先级、数据库连接池生命周期 | `application.yml` (`open-in-view: false`) |
| **02** | `TraceIdFilter`、统一响应与全局异常 | Servlet Filter 链顺序、`ThreadLocal`/`MDC` 复用串号 | `TraceIdFilter` · `FoundationPitfallDrillsTest#drill01` |
| **03** | RBAC 三表模型与 `LAZY + @BatchSize` | 消除 `EAGER` N+1 查询、`@EntityGraph` 单次联表 | `SysUser` / `SysRole` · `FoundationPitfallDrillsTest#drill05` |
| **04** | JWT 双 Token 签名与防类型混淆攻击 | HMAC 验签本质、`ACCESS` vs `REFRESH` 权限隔离 | `JwtService` · `FoundationPitfallDrillsTest#drill02` |
| **05** | Refresh 一次性轮换、黑名单与防爆破 | 原子消费防重放、`ConcurrentHashMap.compute` 计数 | `AuthService` / `LoginAttemptGuard` · `#drill03` |
| **06** | Spring Security 无状态链与 `@PreAuthorize` | 401 vs 403、`SecurityContextHolder` 线程上下文 | `JwtAuthFilter` / `SecurityConfig` · `AuthFlowIntegrationTest` |
| **07** | JPA `Specification` 动态分页与防爆页 | 预编译防注入、分页上限钳制 | `UserService.queryUsers` |
| **08** | 流式分批 CSV 导出、BOM 与防公式注入 | 分批写流防 OOM、UTF-8 BOM、Excel 公式注入转义 | `UserService.exportCsv` · `RbacAndExportIntegrationTest` |
| **09** | AOP `@Audited` 审计切面与 `REQUIRES_NEW` | 动态代理自调用坑、挂起外层回滚事务保失败日志 | `AuditLogPersistenceService` · `FoundationPitfallDrillsTest#drill04` |
| **10** | Docker 多阶段构建、健康编排与 Nginx | 镜像瘦身、非 root 运行、`service_healthy` 启动序 | `Dockerfile` / `docker-compose.yml` / `deploy/nginx.conf` |

---

### 下半场：项目 B — 高并发秒杀与订单履约系统（[`WORKSHOP_B.md`](./project-b-seckill/WORKSHOP_B.md)）

| 节次 | 课程主题 | 补牢哪块基础地基 | 对应源码与可运行演练 |
|---|---|---|---|
| **11** | 秒杀表结构与数据库防超卖铁闸 | InnoDB 行锁条件更新、`(campaign_id, user_id)` 唯一键 | `CouponCampaignRepository` / `db/schema.sql` |
| **12** | 热点详情页：SingleFlight + 空哨兵 + 随机 TTL | 防缓存击穿（请求合并）、防穿透、防雪崩 | `CampaignQueryService` · `SeckillFoundationDrillsTest#drill04` |
| **13** | 16 分片 CAS 预扣 + 顺延探测 + 按人限领 | 热点拆分降自旋、`Math.floorMod` 负哈希防坑 | `LocalShardedPreDeductEngine` |
| **14** | 分布式 Redis Lua 脚本原子预扣 | 单线程脚本消除多命令 RTT 竞态窗口 | `seckill_pre_deduct.lua` / `RedisLuaPreDeductEngine` |
| **15** | 有界线程池反压回滚 + 跨线程 `MDC` 快照 | 防无界队列 OOM、防 `DiscardPolicy` 静默丢库存 | `SeckillExecutorConfig` · `SeckillFoundationDrillsTest#drill01, #drill05` |
| **16** | 本地消息表 `tx-outbox` 同事务落盘 | 解决「写库 + 发 MQ」双写不一致、避免长事务占连接 | `SeckillOrderPersistenceService` / `TxOutbox` |
| **17** | 发件箱定时中继 + 下游唯一键幂等发券 | At-Least-Once 投递重发必然性与 `uk_coupon_order_no` 兜底 | `OutboxRelayScheduler` / `CouponFulfillmentConsumer` · `#drill03` |
| **18** | CAS 订单状态机 + 关单安全回补库存 | 支付 vs 关单互斥、`casWon==1` + `afterCommit()` 回补 | `OrderStateService` · `SeckillFoundationDrillsTest#drill02` |
| **19** | 32 线程高并发对账与吞吐量基准实测 | 账实核对闭环、同步锁 vs 分片异步实测对比 | `SeckillConcurrencyBenchmarkTest` / `SeckillControllerIntegrationTest` |
| **20** | JMeter 压测、Docker 生产编排与简历答辩 | P99/QPS 压测度量、白板画图与 STAR 法则表达 | `jmeter/seckill-benchmark.jmx` / `RESUME_GUIDE.md` |
