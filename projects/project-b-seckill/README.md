# 项目 B：高并发秒杀抢券与订单履约系统（高并发核心亮点）

> 对应模块 7「项目 B」：将模块 0 前 32 课所学并发原语落地为完整 Spring Boot 3 工程，并附带 JMeter 压测脚本与内置并发基准测试。

## 核心架构与课程映射

| 模块 0 实验课 | 在本工程中的真实落地 | 核心文件 |
|---|---|---|
| `#26` 分段扣库存 (`lab.shardstock`) | 16 分片 CAS 桶 + 顺延探测，降低热点竞争 | `LocalShardedPreDeductEngine` / `RedisLuaPreDeductEngine` |
| `#23` 每人限领一次 (`lab.userlock`) | 按用户去重拦截，重放请求直接返回 `ALREADY_CLAIMED` | `seckill_pre_deduct.lua` / `claimedUsers` |
| `#20` 线程池反压 (`lab.poolreject`) | 有界 `ArrayBlockingQueue` + 显式拒绝回滚预扣库存 | `SeckillExecutorConfig` |
| `#32` 发件箱最终一致 (`lab.txoutbox`) | `SeckillOrder` 与 `TxOutbox` 同库同事务落盘 + 定时中继投递 | `SeckillOrderPersistenceService` + `OutboxRelayScheduler` |
| `#24` 状态机 CAS (`lab.orderstate`) | 支付回调 vs 超时关单条件更新，保证互斥成功 | `OrderStateService` + `SeckillOrderRepository` |

## 运行与并发压测

```bash
# 运行内置高并发压测与正确性验证（零外部依赖，H2 + 内存分片引擎）
mvn test

# 启动服务（端口 8081）
mvn spring-boot:run

# 使用 JMeter 对本地服务发起 200 并发 × 10 轮压测
jmeter -n -t jmeter/seckill-benchmark.jmx -l result.jtl
```
