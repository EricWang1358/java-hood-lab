# 模块 7：20 节实战拆解工坊（从空目录到上线压测）

> 建议节奏：先对照模块 0 的 32 节并发实验课，再按本工坊 20 节顺序亲手敲出或拆解两个工程，每完成一节跑通对应的自动化测试。

---

## 上半场：项目 A — 企业级 RBAC 后台系统（第 1～10 节）

| 节次 | 主题 | 核心代码定位 | 手敲/拆解要点与常见坑 |
|---|---|---|---|
| **01** | Maven 脚手架与环境隔离 | `pom.xml`、`application.yml`、`application-mysql.yml` | 本地默认用 H2 `MODE=MySQL` + `open-in-view: false`，生产切 `mysql` profile。 |
| **02** | 统一响应体与全局异常 | `ApiResponse`、`ErrorCode`、`GlobalExceptionHandler` | 把业务异常 `BizException`、参数校验 `MethodArgumentNotValidException` 收口为统一错误码。 |
| **03** | RBAC 三表模型与初始化 | `SysUser`、`SysRole`、`SysPermission`、`DataInitializer` | 多对多关系不要在实体上用 `CascadeType.ALL` 误删基础角色；注意密码必须 BCrypt 加密。 |
| **04** | JWT 双 Token 签发 | `JwtService`、`JwtProperties` | `ACCESS` 与 `REFRESH` 必须带独立 `type` claim，防止拿 Refresh Token 直接调业务接口。 |
| **05** | Token 轮换与黑名单吊销 | `TokenStore`、`InMemoryTokenStore`、`RedisTokenStore`、`AuthService` | Refresh Token 使用一次即消费（`getAndDelete`），旧 Token 重放直接报 `40102`；Logout 将 Access `jti` 写入黑名单至剩余 TTL。 |
| **06** | 无状态过滤器与 RBAC 鉴权 | `JwtAuthFilter`、`SecurityConfig`、`UserController` | `@EnableMethodSecurity` 开启 `@PreAuthorize("hasAuthority('user:write')")`；区分 401（未登录）与 403（无权限）。 |
| **07** | JPA 动态分页检索 | `UserService.queryUsers`、`PageResult` | 用 `Specification` 动态拼接关键字与状态过滤，限制单页最大 `size <= 100` 防深分页拖垮数据库。 |
| **08** | 流式 CSV 导出与安全防护 | `UserService.exportCsv` | 分页切片流式写 `Writer` 防 OOM；写 UTF-8 BOM 兼容 Excel；对 `= + - @` 开头单元格转义防 CSV 公式注入。 |
| **09** | Spring AOP 操作审计日志 | `@Audited`、`AuditLogAspect`、`RoleController` | 用 `@Around` 统计耗时并记录操作人、方法与成败；跑通 `AuditAndRoleIntegrationTest`。 |
| **10** | Docker 多阶段构建与 Nginx | `Dockerfile`、`docker-compose.yml`、`deploy/nginx.conf` | 多阶段构建裁剪镜像体积（仅含 JRE 17）；`depends_on: condition: service_healthy` 等 MySQL/Redis 就绪再启动。 |

---

## 下半场：项目 B — 高并发秒杀与订单履约系统（第 11～20 节）

| 节次 | 主题 | 核心代码定位 | 手敲/拆解要点与常见坑 |
|---|---|---|---|
| **11** | 秒杀域模型与防超卖底线 | `CouponCampaign`、`SeckillOrder`、`db/schema.sql` | 数据库层加 `UNIQUE KEY uk_campaign_user(campaign_id, user_id)` 与 `WHERE remaining_stock > 0` 兜底。 |
| **12** | 16 分片 CAS 库存预扣 | `LocalShardedPreDeductEngine` | 对应实验课 `#26`：把单点库存拆进 16 个桶，先按 `userId` 哈希落桶，空桶则顺延探测，降低锁/CAS 竞争 16 倍。 |
| **13** | 每人限领一次原子拦截 | `LocalShardedPreDeductEngine`、`claimedUsers` | 对应实验课 `#23`：先原子占位再扣桶，若全桶售罄需回滚用户占位，保持状态一致。 |
| **14** | Redis Lua 脚本原子预扣 | `seckill_pre_deduct.lua`、`RedisLuaPreDeductEngine` | 单次 Lua 脚本内完成 `SISMEMBER` 判重 + `GET` 判余量 + `DECR` + `SADD`，消除网络 RTT 与竞态窗口。 |
| **15** | 有界线程池与显式反压 | `SeckillExecutorConfig`、`SeckillService` | 对应实验课 `#20`：用有界 `ArrayBlockingQueue`，队列满时抛 `RejectedExecutionException` 并立即回滚预扣库存，返回 HTTP `429`。 |
| **16** | `tx-outbox` 同事务落盘 | `SeckillOrderPersistenceService`、`TxOutbox` | 对应实验课 `#32`：订单表与本地消息表必须在**同一个 `@Transactional`** 内提交，且独立 Service 避免同类自调用失效。 |
| **17** | 发件箱定时中继与幂等投递 | `OutboxRelayScheduler`、`InMemoryOrderEventPublisher` | 定时扫描 `PENDING` 事件投递并更新 `SENT`，失败累计重试次数，消费端按 `eventId` 幂等去重。 |
| **18** | CAS 订单状态机防撞车 | `OrderStateService`、`SeckillOrderRepository` | 对应实验课 `#24`：`UPDATE ... SET status = :target WHERE order_no = :no AND status = :expected`，保证支付回调与超时关单互斥成功。 |
| **19** | 全链路集成与并发基准验证 | `SeckillConcurrencyBenchmarkTest`、`SeckillControllerIntegrationTest` | 验证 32 线程 1600 次并发下零超卖、零重领、`tx_outbox` 100% 投递、以及 `428 req/s` vs `50,000+ req/s` 吞吐对比。 |
| **20** | JMeter 压测与简历量化 | `jmeter/seckill-benchmark.jmx`、`RESUME_GUIDE.md` | 跑通 200 并发压测，把架构演进、实测 QPS 与排障思考沉淀为 STAR 简历话术。 |
