# 项目 A：企业级 RBAC 后台管理系统（工程规范底座）

> 对应模块 7「项目 A」（工坊第 01～10 节）：Spring Boot 3.3.5 + Java 17 企业级后台底座。
> - **零基础先看地基透视图**：[`../FOUNDATIONS_MAP.md`](../FOUNDATIONS_MAP.md)
> - **第 01～10 节逐节手敲与破坏实验手册**：[`./WORKSHOP_A.md`](./WORKSHOP_A.md)
> - **5 组「新手踩坑 vs 生产修复」可运行断点演练**：[`FoundationPitfallDrillsTest.java`](./src/test/java/com/javahood/admin/drills/FoundationPitfallDrillsTest.java)

## 核心工程特性

1. **全链路 `TraceId` 透传与防串号**：
   - `TraceIdFilter` 站在 Servlet Filter 最前排绑定 `X-Trace-Id` 至 `MDC`，`finally` 强制清理防 Tomcat 线程复用串号；`ApiResponse` 与审计日志自动携带 `traceId`。
2. **JWT 双 Token 轮换、吊销与防爆破**：
   - `POST /api/auth/login` 前置 `LoginAttemptGuard`（`ConcurrentHashMap.compute` 原子统计，连错 5 次锁定 `42901`），签发短效 `accessToken`（15m）+ 长效 `refreshToken`（7d，强制隔离 `type` claim 防类型混淆）。
   - `POST /api/auth/refresh` 一次性原子消费旧 `jti` 并轮换新 Token 对，重放旧 Token 返回 `40102`。
   - `POST /api/auth/logout` 按剩余 TTL 拉黑 Access `jti` 并销毁 Refresh Token（支持内存 / Redis 零代码切换）。
3. **RBAC 三级权限与 JPA N+1 治理**：
   - 关闭 OSIV（`open-in-view: false`），`SysUser` ↔ `SysRole` ↔ `SysPermission` 全部采用 `FetchType.LAZY + @BatchSize(50)` 配合登录单查 `@EntityGraph`，彻底消灭分页 N+1 查询与懒加载异常。
   - 默认内置 3 个演示账号：`admin/admin123`、`operator/oper123`、`viewer/view123`。
4. **动态分页、防注入流式 CSV 导出与 `REQUIRES_NEW` 独立事务审计**：
   - `GET /api/users` 基于 JPA `Specification` 动态查询并钳制单页上限。
   - `GET /api/users/export` 分批流式输出 UTF-8 BOM CSV，并对 `= + - @` 前缀字段做 Excel 公式注入转义。
   - `@Audited` AOP 切面委托独立 Bean `AuditLogPersistenceService`（`Propagation.REQUIRES_NEW`），即使外层业务事务抛异常回滚，失败现场审计日志仍独立落盘。

## 本地运行与测试（共 8 项集成与基础演练测试）

```bash
# 一键跑通全部集成测试 + 5 组基础踩坑对照演练（H2 MySQL 兼容模式，零外部依赖）
mvn test

# 只运行基础夯实演练（强烈建议在 IDE 中 Debug 单步运行）
mvn test -Dtest=FoundationPitfallDrillsTest

# 本地启动服务（端口 8080）
mvn spring-boot:run
```

## Docker / MySQL 8 / Redis 7 / Nginx 生产部署

```bash
cp .env.example .env
docker compose up --build -d
```
