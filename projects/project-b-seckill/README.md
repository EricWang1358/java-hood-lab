# 项目 B：高并发秒杀抢券与订单履约系统（高并发核心亮点）

> 对应模块 7「项目 B」（工坊第 11～20 节）：把前序并发课、MySQL 事务锁课与 Redis/MQ 课真正焊成一套完整闭环的 Spring Boot 3 高并发工程。
> - **先看五道防线全景透视图**：[`../FOUNDATIONS_MAP.md`](../FOUNDATIONS_MAP.md)
> - **第 11～20 节逐节手敲与破坏实验手册**：[`./WORKSHOP_B.md`](./WORKSHOP_B.md)
> - **5 组高并发与一致性「踩坑 vs 修复」断点演练**：[`SeckillFoundationDrillsTest.java`](./src/test/java/com/javahood/seckill/drills/SeckillFoundationDrillsTest.java)

## 五道核心防线与实验课映射

| 防线层级 | 前序实验课 | 在本工程中的生产级落地 | 核心代码定位 |
|---|---|---|---|
| **1. 热点详情读保护** | `#25` + 模块 6 | SingleFlight 请求合并防击穿 + 空值哨兵防穿透 + 随机 TTL 防雪崩 | `CampaignQueryService` |
| **2. 分片原子预扣** | `#23`, `#26` + 模块 6 | 16 分片 CAS 桶 / Redis Lua 单脚本原子执行「按人限领 + 顺延扣减」 | `LocalShardedPreDeductEngine` / `RedisLuaPreDeductEngine` / `seckill_pre_deduct.lua` |
| **3. 有界反压与异步追踪** | `#19`, `#20` | 有界 `ArrayBlockingQueue` 饱和抛异常并回滚预扣库存 + 跨线程 `MDC` 快照 | `SeckillExecutorConfig` / `SeckillService` |
| **4. 发件箱与幂等发券** | `#32` + 模块 6 | 扣库 + 订单 + `tx_outbox` 同事务落盘 -> 定时中继 -> `uk_coupon_order_no` 幂等发券 | `SeckillOrderPersistenceService` / `OutboxRelayScheduler` / `CouponFulfillmentConsumer` |
| **5. 状态机与超时回补** | `#24` + 模块 5 | 支付 vs 关单 SQL CAS 互斥；仅 `casWon==1` 时回补 DB 库存并在 `afterCommit()` 归还分片池 | `OrderStateService` / `SeckillOrderRepository` |

## 本地运行与压测（共 9 项并发对账、HTTP 集成与踩坑演练测试）

```bash
# 一键运行全部高并发对账压测 + HTTP 全链路测试 + 5 组基础演练（零外部依赖）
mvn test

# 单独 Debug 运行 5 组高并发与一致性基础演练
mvn test -Dtest=SeckillFoundationDrillsTest

# 本地启动服务（端口 8081）
mvn spring-boot:run

# 使用 JMeter 发起 200 并发压测
jmeter -n -t jmeter/seckill-benchmark.jmx -l result.jtl
```

## Docker / MySQL 8 / Redis 7 生产部署

```bash
docker compose up --build -d
```
