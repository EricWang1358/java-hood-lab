# 模块 7：简历实战项目 × 2（工程拆解工坊）

本目录沉淀了 Java 后端入职线的两个核心实战工程，均基于 **Java 17 + Spring Boot 3.3.5 + Maven** 构建，默认支持零外部依赖一键运行 `mvn test`，同时提供 MySQL / Redis / Docker 生产配置与 JMeter 压测脚本。

## 项目总览

1. **[`project-a-admin`](./project-a-admin/)（项目 A — 工程规范底座）**
   - **技术栈**：Spring Boot 3 + Spring Security + JPA + MySQL/H2 + Redis + JJWT + Docker
   - **核心能力**：JWT 双 Token 轮换与黑名单吊销、RBAC 三级权限模型、动态分页检索、UTF-8 BOM + 防公式注入流式 CSV 导出。
2. **[`project-b-seckill`](./project-b-seckill/)（项目 B — 高并发核心亮点）**
   - **技术栈**：Spring Boot 3 + 分片原子预扣（本地 CAS / Redis Lua）+ 有界线程池反压 + `tx-outbox` 本地消息表 + CAS 订单状态机 + JMeter
   - **核心能力**：把模块 0（32 节并发实验课）中的**分段防超卖、按人限领、线程池反压、发件箱最终一致、订单状态机 CAS**串成完整高并发工程，并内置吞吐量对比测试。

## 20 节拆解工坊推进路线

| 阶段 | 节次 | 对应工程任务 | 验收标准 |
|---|---|---|---|
| **工坊 1～4** | 第 1～4 节 | 脚手架、统一响应体、全局异常、MySQL Schema | `mvn test` 启动成功 |
| **工坊 5～8** | 第 5～8 节 | JWT 双 Token 签发/轮换/吊销、RBAC 权限拦截 | `AuthFlowIntegrationTest` 通过 |
| **工坊 9～10** | 第 9～10 节 | JPA 动态分页、流式 CSV 导出与 Docker 编排 | `RbacAndExportIntegrationTest` 通过 |
| **工坊 11～14** | 第 11～14 节 | 秒杀活动建模、16 分片 CAS / Redis Lua 预扣、按人限领 | 高并发零超卖、零重领 |
| **工坊 15～18** | 第 15～18 节 | 有界线程池反压、`tx-outbox` 同事务落盘与中继、CAS 状态机 | `SeckillConcurrencyBenchmarkTest` 通过 |
| **工坊 19～20** | 第 19～20 节 | JMeter 压测前后 QPS 对比与简历 STAR 量化沉淀 | 输出真实 QPS / P99 指标 |
