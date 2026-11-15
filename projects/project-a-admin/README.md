# 项目 A：企业级 RBAC 后台管理系统（工程规范底座）

> 对应模块 7「项目 A」：Spring Boot 3 + MySQL + Redis 后台系统（JWT 双 Token 轮换、RBAC 细粒度权限、动态分页检索与防注入流式 CSV 导出、Docker/Linux 部署）。

## 核心工程特性

1. **JWT 双 Token 鉴权与吊销机制**：
   - `POST /api/auth/login` 签发短效 `accessToken`（15 分钟）与长效 `refreshToken`（7 天，带独立 `jti`）。
   - `POST /api/auth/refresh` 执行一次性 Refresh Token 轮换（消费旧 `jti` 并签发新对），重放旧 Refresh Token 返回 `40102`。
   - `POST /api/auth/logout` 将当前 Access Token `jti` 写入黑名单直到过期，并销毁对应 Refresh Token。
   - `TokenStore` 默认使用内存实现以便零依赖本地测试；生产开启 `app.redis.enabled=true` 自动切换为 `RedisTokenStore`。
2. **RBAC 权限模型**：
   - `sys_user` ↔ `sys_role` ↔ `sys_permission` 三级模型，通过 `@PreAuthorize("hasAuthority('user:write')")` 控制接口权限。
   - 默认初始化 3 个测试账号：`admin/admin123`、`operator/oper123`、`viewer/view123`。
3. **动态分页与流式 CSV 导出**：
   - `GET /api/users?keyword=...&enabled=...&page=0&size=10` 基于 JPA `Specification` 动态查询。
   - `GET /api/users/export` 分批流式写入 CSV，自动附加 UTF-8 BOM（兼容 Excel），并对 `= + - @` 开头字段做公式注入转义。

## 本地运行与测试

```bash
# 运行完整集成测试（使用 H2 MySQL 兼容模式 + 内存 TokenStore，无需外部依赖）
mvn test

# 本地启动服务（端口 8080）
mvn spring-boot:run
```

## Docker / MySQL / Redis 生产部署

```bash
docker compose up --build -d
```
