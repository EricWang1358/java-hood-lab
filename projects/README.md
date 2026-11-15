# 模块 7：简历实战项目 × 2（地基夯实 + 20 节交互拆解工坊）

本目录提供从**底层原理串讲 → 正反踩坑对照演练 → 生产级完整工程 → 2 分钟脱稿面试答辩**的完整闭环，均基于 **Java 17 + Spring Boot 3.3.5 + Maven** 构建，默认支持**零外部依赖**一键运行 `mvn test`，同时配备真实 MySQL 8 / Redis 7 / Docker / Nginx 生产编排与 JMeter 压测脚本。

---

## 四步把地基打牢（强烈推荐学习顺序）

1. **第一步：读透底层桥梁图谱（告别「看框架像黑盒」）**
   - 精读 [`FOUNDATIONS_MAP.md`](./FOUNDATIONS_MAP.md)：一次串通 Tomcat 工作线程池、`OncePerRequestFilter`、`ThreadLocal`/`MDC`、Spring CGLIB 动态代理洋葱圈、JPA `open-in-view: false` + `LAZY + @BatchSize` 与秒杀五道防线。
2. **第二步：打开零依赖交互式实训工作台（边猜边看机盖底下）**
   - 本地双击打开 [`workshop-lab.html`](./workshop-lab.html)：内置**项目 A / B 请求全链路拦截模拟器** + **20 节逐节直觉快测、正反代码对照、破坏实验指引与 120 秒开口倒计时器**。
3. **第三步：在 IDE 单步 Debug 10 组「踩坑 vs 修复」实证测试**
   - 项目 A 演练：[`FoundationPitfallDrillsTest.java`](./project-a-admin/src/test/java/com/javahood/admin/drills/FoundationPitfallDrillsTest.java) + 逐节手册 [`WORKSHOP_A.md`](./project-a-admin/WORKSHOP_A.md)（第 01～10 节）
   - 项目 B 演练：[`SeckillFoundationDrillsTest.java`](./project-b-seckill/src/test/java/com/javahood/seckill/drills/SeckillFoundationDrillsTest.java) + 逐节手册 [`WORKSHOP_B.md`](./project-b-seckill/WORKSHOP_B.md)（第 11～20 节）
4. **第四步：量化上简历与二面连环追问脱稿演练**
   - 对照 [`WORKSHOP.md`](./WORKSHOP.md) 与 [`RESUME_GUIDE.md`](./RESUME_GUIDE.md)（含 STAR 量化话术与 Top 12 二面连环深挖满分答法）。

---

## 两大实战工程概览

| 工程目录 | 定位 | 核心工程与底层亮点 | 自动化验证（零外置依赖） |
|---|---|---|---|
| **[`project-a-admin`](./project-a-admin/)** | **项目 A：企业级 RBAC 后台底座** | `TraceIdFilter` 防复用串号、JWT 双 Token 轮换 + `type` 防混淆 + 剩余 TTL 黑名单、`LoginAttemptGuard` 原子防爆破、三级 RBAC、关 OSIV + `LAZY/@BatchSize/@EntityGraph` 灭 N+1、防公式注入流式 CSV、`REQUIRES_NEW` 独立事务保失败审计日志、Docker/Nginx | `mvn test` **8/8** 通过 |
| **[`project-b-seckill`](./project-b-seckill/)** | **项目 B：高并发秒杀与履约系统** | 详情页 SingleFlight + 空哨兵 + 随机 TTL、16 分片 CAS / Redis Lua 原子限购预扣、有界池满队回滚预扣 + 跨线程 `MDC` 快照、同事务 `tx-outbox` + 下游 `uk_coupon_order_no` 幂等发券、支付 vs 关单 CAS 互斥 + `afterCommit` 安全回补库存、JMeter + Docker | `mvn test` **9/9** 通过（峰值吞吐 `50,000+ req/s`） |
