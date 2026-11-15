# 项目 A 逐节深潜手册（第 01～10 节：企业级 RBAC 后台底座）

> 阅读本手册前，建议先花 15 分钟读完 [`../FOUNDATIONS_MAP.md`](../FOUNDATIONS_MAP.md) 第一～三节。
> 每一节都按 **【底层地基】→【新手坏味道】→【本仓源码深潜】→【亲手破坏与断点任务】→【开口自检】** 五步走，拒绝走马观花。

---

## 第 01 节：Maven 工程骨架、配置隔离与关闭 OSIV

- **【底层地基】**：
  - Spring Boot 启动时会按 `application.yml` → `application-{profile}.yml` → 环境变量的优先级覆盖配置。
  - `spring.jpa.open-in-view` 默认竟然是 `true`（即 Open Session In View）：它会在请求刚进 Controller 前就占住一条数据库连接，直到整个视图/JSON 写出完毕才释放。生产高并发下**必须显式设为 `false`**，让数据库连接只在 `@Transactional` 方法执行 SQL 的几毫秒内被占用。
- **【新手坏味道】**：把生产数据库密码硬编码进 `application.yml` 提交到 Git；保留默认 `open-in-view: true` 掩盖懒加载问题。
- **【本仓源码深潜】**：
  - 查看 [`pom.xml`](./pom.xml)、[`application.yml`](./src/main/resources/application.yml)、[`application-mysql.yml`](./src/main/resources/application-mysql.yml)。
  - 本地默认使用 H2 的 `MODE=MySQL` 内存模式启动，让你无需装任何外部服务即可跑通全部测试；生产通过 `SPRING_PROFILES_ACTIVE=mysql` 切换真实 MySQL + Redis。
- **【亲手破坏与断点任务】**：
  - 检查 `application.yml` 第 13 行 `open-in-view: false`，记住这个配置——到第 03 节和第 07 节你会亲眼看到它如何逼着我们写出干净的事务边界。
- **【开口自检】**：为什么公司规范强制要求把 `spring.jpa.open-in-view` 关掉？不关掉在高并发慢网络下会把什么资源耗尽？

---

## 第 02 节：全链路 `TraceId` 过滤器、统一响应体与全局异常收口

- **【底层地基】**：
  - 请求还没进 Spring MVC 的 `DispatcherServlet` 之前，会先经过 **Servlet `Filter` 链**；进了 `DispatcherServlet` 之后抛出的业务异常，才会被 `@RestControllerAdvice` 捕获。
  - 日志追踪号 `traceId` 放在 SLF4J 的 `MDC` 里，底层是 `ThreadLocal`。因为 Tomcat 线程池会反复复用工作线程，**Filter 的 `finally` 块里必须执行 `MDC.remove("traceId")`**。
- **【新手坏味道】**：每个 Controller 方法里都写一堆 `try { ... } catch (Exception e) { return map; }`；出线上 Bug 时几千行并发日志混在一起，根本分不清哪几行属于同一个报错请求。
- **【本仓源码深潜】**：
  - [`TraceIdFilter.java`](./src/main/java/com/javahood/admin/common/TraceIdFilter.java)：`@Order(Ordered.HIGHEST_PRECEDENCE)` 站在最前排生成/读取 `X-Trace-Id`，回写响应头，并在 `finally` 清理。
  - [`ApiResponse.java`](./src/main/java/com/javahood/admin/common/ApiResponse.java)：自动从 `MDC` 提取 `traceId` 塞进每一份 JSON 返回体。
  - [`GlobalExceptionHandler.java`](./src/main/java/com/javahood/admin/common/GlobalExceptionHandler.java)：集中把 `BizException`、`MethodArgumentNotValidException`（`@Valid` 参数错误）翻译成标准 HTTP 状态码与业务错误码。
- **【亲手破坏与断点任务】**：
  1. 打开 [`FoundationPitfallDrillsTest.java`](./src/test/java/com/javahood/admin/drills/FoundationPitfallDrillsTest.java) 运行 `drill01_threadLocalMdc_leakWithoutFinally_vs_safeTraceIdFilter`。
  2. 试着把 `TraceIdFilter.java` 第 37 行的 `MDC.remove(MDC_TRACE_KEY);` 注释掉，再跑一次 `drill01`，观察主线程残留的脏 `traceId` 断言当场变红！测完记得恢复。
- **【开口自检】**：Filter 里抛出的异常能被 `@RestControllerAdvice` 捕获吗？为什么？（提示：回头看地基图谱的第一张洋葱图，谁包在谁外面？）

---

## 第 03 节：RBAC 三级权限表建模与 JPA `LAZY + @BatchSize` 调优

- **【底层地基】**：
  - 为什么不把角色直接写成用户表里的一个字符串字段 `role = "ADMIN"`？因为一旦业务增加「运营也能导出但不能删人」的需求，硬编码角色判断就会改崩全站。标准 **RBAC（用户 ↔ 角色 ↔ 权限点）** 让接口只认细粒度权限码（如 `user:export`），角色可自由组合权限。
  - JPA 多对多关联默认是 `FetchType.LAZY`，千万别图省事改成 `EAGER`（会引发 N+1 SQL 灾难）。
- **【新手坏味道】**：`@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)` —— 不仅每次查列表触发几十条关联 SQL，一旦删一个用户还可能级联把公共的 `ROLE_ADMIN` 给删了！
- **【本仓源码深潜】**：
  - [`SysUser.java`](./src/main/java/com/javahood/admin/domain/SysUser.java)、[`SysRole.java`](./src/main/java/com/javahood/admin/domain/SysRole.java)、[`SysPermission.java`](./src/main/java/com/javahood/admin/domain/SysPermission.java)。
  - [`SysUserRepository.java`](./src/main/java/com/javahood/admin/repository/SysUserRepository.java)：登录单查用户时用 `@EntityGraph(attributePaths = {"roles", "roles.permissions"})` 一条 `LEFT JOIN` 查齐；分页查列表时靠实体上的 `@BatchSize(size = 50)` 用 `WHERE user_id IN (...)` 批量拉取。
- **【亲手破坏与断点任务】**：
  - 打开 `UserService.java` 第 70 行，把 `queryUsers` 头上的 `@Transactional(readOnly = true)` 临时注释掉，然后运行 `FoundationPitfallDrillsTest#drill05` —— 你会立刻亲眼见到著名的 `LazyInitializationException`！弄明白「因为 `open-in-view: false`，脱离 `@Transactional` 后 Session 已关闭」后，把注解恢复回来。
- **【开口自检】**：为什么分页查询不能直接对一对多/多对多集合做 `JOIN FETCH`？（提示：主表 1 行变多行后，SQL 的 `LIMIT 10` 截断的是连接后的行还是主表用户数？Hibernate 会被迫干什么傻事？）

---

## 第 04 节：JWT 双 Token 签名原理与「类型混淆攻击」防御

- **【底层地基】**：
  - JWT 由 `Header.Payload.Signature` 三段 Base64Url 组成。**Payload 只是编码不是加密，任何人都能解码看到内容**！服务端靠 `HMAC-SHA256(Header + "." + Payload, SecretKey)` 重新算签名来确认「内容没被篡改」。
  - 为什么要分短效 `Access Token`（15 分钟）和长效 `Refresh Token`（7 天）？因为 Access Token 每次请求都带在 Header 里高频暴露，短寿命能把泄露窗口压到最小；Refresh Token 只在刷新时用一次。
- **【新手坏味道】**：签发 Access 和 Refresh 时用同一把密钥，只改了过期时间，没写 `type` 字段或解析时不校验 `type` —— 导致 7 天的 Refresh Token 可以直接当 Access Token 调所有业务接口！
- **【本仓源码深潜】**：
  - [`JwtService.java`](./src/main/java/com/javahood/admin/security/JwtService.java)：签发时写入 `.claim("type", TokenType.ACCESS.name())` 与唯一 ID `jti`，`parseAndValidate(token, expectedType)` 强制核对 `type`。
- **【亲手破坏与断点任务】**：
  - 在 `JwtService.parseAndValidate` 打断点，运行 `FoundationPitfallDrillsTest#drill02`，单步查看解码出来的 `Claims` 对象里面长什么样（`sub`、`jti`、`type`、`exp`）。
- **【开口自检】**：能在 JWT Payload 里存用户的手机号或明文密码吗？为什么？如果服务端密钥没泄露，客户端把 Payload 里的 `"sub": "viewer"` 改成 `"admin"` 发过来会在哪一步失败？

---

## 第 05 节：Refresh 一次性轮换（Rotation）、登出黑名单与防暴力破解

- **【底层地基】**：
  - 纯无状态 JWT 最大的痛点是「签出去就像泼出去的水，过期前收不回来」。
  - 工程解法是**「99% 读请求纯内存验签 + 极低频登出/刷新查 Redis 状态」**：
    1. 每次签发 Refresh Token 时把 `auth:refresh:{jti} -> username` 存入 `TokenStore`；刷新时执行**原子读取并删除（`getAndDelete` / `remove`）**，消费掉旧的再签发新的一对。一旦旧 Refresh Token 被二次重放，立即报 `40102`！
    2. 登出时把当前 Access Token 的 `jti` 写入 `auth:blacklist:{jti}`，TTL 仅需设为**该 Access Token 剩余存活秒数**（最多 15 分钟就自动清干净了，绝不撑爆 Redis）。
    3. 登录口前置 [`LoginAttemptGuard`](./src/main/java/com/javahood/admin/security/LoginAttemptGuard.java)，利用 `ConcurrentHashMap.compute()` 原子累计密码错误次数，连错 5 次直接锁定返回 `42901`，保护数据库与 BCrypt CPU。
- **【本仓源码深潜】**：
  - [`TokenStore.java`](./src/main/java/com/javahood/admin/security/TokenStore.java)、[`InMemoryTokenStore.java`](./src/main/java/com/javahood/admin/security/InMemoryTokenStore.java)、[`RedisTokenStore.java`](./src/main/java/com/javahood/admin/security/RedisTokenStore.java)、[`AuthService.java`](./src/main/java/com/javahood/admin/service/AuthService.java)。
- **【亲手破坏与断点任务】**：
  - 运行 [`AuthFlowIntegrationTest.java`](./src/test/java/com/javahood/admin/AuthFlowIntegrationTest.java) 和 `FoundationPitfallDrillsTest#drill03`，观察完整「登录 → 访问 → 轮换刷新 → 重放旧 Refresh 被拒 → 登出拉黑 Access → 再访问报 401」全生命周期。
- **【开口自检】**：为什么登出黑名单的 Redis Key 过期时间设成「Access Token 剩余有效期」就够了，不需要永久保存？

---

## 第 06 节：Spring Security 无状态过滤器链与 `@PreAuthorize` 鉴权

- **【底层地基】**：
  - 认证（Authentication，你是谁 -> 未过报 **401**）和授权（Authorization，你能干啥 -> 未过报 **403**）是两码事！
  - `JwtAuthFilter` 验完 Access Token 且确认不在黑名单后，构造 `UsernamePasswordAuthenticationToken(username, null, authorities)` 放进 `SecurityContextHolder`。
  - 随后进入 Controller 代理层时，`@PreAuthorize("hasAuthority('user:write')")` 会由 Spring AOP 拦截器读取 `SecurityContextHolder` 里的权限列表，不含 `'user:write'` 就抛 `AccessDeniedException`（转 403）。
- **【本仓源码深潜】**：
  - [`JwtAuthFilter.java`](./src/main/java/com/javahood/admin/security/JwtAuthFilter.java)、[`SecurityConfig.java`](./src/main/java/com/javahood/admin/security/SecurityConfig.java)、[`UserController.java`](./src/main/java/com/javahood/admin/controller/UserController.java)。
- **【亲手破坏与断点任务】**：
  - 在 `JwtAuthFilter.doFilterInternal` 和 `UserController.create` 各打一个断点，运行 [`RbacAndExportIntegrationTest.java`](./src/test/java/com/javahood/admin/RbacAndExportIntegrationTest.java)，对比 `viewer` 账号（只有 `user:read`）和 `admin` 账号调用栈里的 `Authentication.authorities` 集合。
- **【开口自检】**：为什么在前后端分离的无状态 JWT 架构里，`SecurityConfig` 可以安全地把 `.sessionCreationPolicy(STATELESS)` 打开并把 `csrf` 关掉？（提示：CSRF 攻击依赖浏览器的什么自动携带机制？Bearer Header 会被跨站自动携带吗？）

---

## 第 07 节：JPA `Specification` 动态多条件分页与防深分页保护

- **【底层地基】**：
  - 后台列表页经常有「关键字可填可不填、启用状态可筛选可不筛选」。用 JPA `Specification`（Criteria API）只把非空条件拼进 `WHERE` 子句，生成的 SQL 全是 `?` 占位符预编译参数，天然杜绝 SQL 注入。
  - 必须对客户端传入的 `size` 做硬上限钳制：`Math.min(Math.max(1, size), 100)`，防止恶意传 `size=10000000` 一把拖垮 JVM 堆内存。
- **【本仓源码深潜】**：
  - [`UserService.java`](./src/main/java/com/javahood/admin/service/UserService.java) 中的 `queryUsers` 与 `buildSpec`、[`PageResult.java`](./src/main/java/com/javahood/admin/common/PageResult.java)。
- **【开口自检】**：如果以后用户表涨到 500 万行，运营翻到第 10000 页（`LIMIT 200000, 20`）变慢了，结合你在模块 4 学的知识，该怎么把偏移量分页改成游标分页（Seek Pagination）或延迟关联？

---

## 第 08 节：流式分批 CSV 导出、UTF-8 BOM 与防公式注入（CSV Injection）

- **【底层地基】**：
  - **内存地基**：导出 10 万条记录时，如果先 `findAll()` 装进一个大 `List<SysUser>` 再拼大字符串，老年代瞬间塞满触发 Full GC 甚至 OOM。正确做法是按每批 200 条循环查出、立刻往 `HttpServletResponse.getWriter()` 流式刷出，内存占用永远只有 200 条！
  - **编码地基**：Windows 版 Excel 打开普通 UTF-8 CSV 会用 GBK 解码导致中文全乱码；在文件最开头写入 3 字节 **UTF-8 BOM (`\uFEFF`)**，Excel 就能准确识别 UTF-8。
  - **安全地基**：如果黑客把自己的昵称注册成 `=cmd|'/C calc'!A0`，运营导出 CSV 用 Excel 双击打开时，Excel 会把 `= + - @` 开头的格当成公式执行！前置一个单引号 `'` 并按 RFC 4180 转义双引号即可彻底防御。
- **【本仓源码深潜】**：
  - [`UserService.java`](./src/main/java/com/javahood/admin/service/UserService.java) 中的 `exportCsv` 与 `csvCell`。
- **【亲手破坏与断点任务】**：
  - 查看 `RbacAndExportIntegrationTest` 里 `displayName = "=SUM(1,2)"` 的导出断言，试着把 `csvCell` 里的公式前置单引号逻辑注释掉，看测试如何捕获这个安全漏洞！
- **【开口自检】**：讲出后台导出功能的三个生产级细节：怎么防 OOM？怎么防 Excel 中文乱码？怎么防 Excel 公式注入？

---

## 第 09 节：Spring AOP `@Audited` 审计切面与 `REQUIRES_NEW` 独立事务

- **【底层地基】**：
  - 对应模块 5 最核心的两个原理：**AOP 环绕通知 `@Around`** 与 **事务传播行为 `Propagation.REQUIRES_NEW`**。
  - 审计日志不仅要记成功操作，**更要记失败/报错的操作**！但当被审计的方法运行在 `@Transactional` 中并抛异常回滚时，如果保存审计日志共用同一个事务（默认 `REQUIRED`），审计日志就会跟着一起回滚丢失！
- **【本仓源码深潜】**：
  - [`Audited.java`](./src/main/java/com/javahood/admin/audit/Audited.java)、[`AuditLogAspect.java`](./src/main/java/com/javahood/admin/audit/AuditLogAspect.java)、[`AuditLogPersistenceService.java`](./src/main/java/com/javahood/admin/audit/AuditLogPersistenceService.java)。
- **【亲手破坏与断点任务】**：
  1. 打开 `AuditLogPersistenceService.java` 第 27 行，把 `@Transactional(propagation = Propagation.REQUIRES_NEW)` 改成普通的 `@Transactional`（即默认 `REQUIRED`）。
  2. 运行 `FoundationPitfallDrillsTest#drill04_auditLogRequiresNew_survivesOuterTransactionRollback` —— 你会亲眼看见异常发生后，失败审计日志人间蒸发、测试当场报错！亲手改回 `REQUIRES_NEW` 体会那种豁然开朗的感觉。
- **【开口自检】**：为什么 `saveInNewTransaction` 不能直接写在 `AuditLogAspect` 自己里面当私有/内部方法调，而必须拆成独立的 `AuditLogPersistenceService` Bean？

---

## 第 10 节：Docker 多阶段构建、健康检查编排与 Nginx 反向代理

- **【底层地基】**：
  - 编译 Java 需要庞大的 Maven + JDK（约 700MB），但运行打好的 Jar 包只需要精简 JRE（Alpine 镜像约 180MB）且不应以 `root` 用户裸跑。**Docker 多阶段构建（Multi-stage build）** 让构建环境与运行环境彻底分离。
  - 容器编排里 `depends_on: [mysql]` 默认只管「容器进程启动了」，不管「MySQL 3306 端口已经准备好接客了」——必须配 `healthcheck` + `condition: service_healthy`，否则 Spring Boot 启动太快会连不上库直接崩退。
- **【本仓源码深潜】**：
  - [`Dockerfile`](./Dockerfile)、[`docker-compose.yml`](./docker-compose.yml)、[`deploy/nginx.conf`](./deploy/nginx.conf)、[`db/schema.sql`](./db/schema.sql)。
- **【开口自检】**：`Dockerfile` 里为什么要专门 `adduser -S appuser` 切换非 root 用户，并显式指定 `-XX:+UseG1GC -Xms256m -Xmx512m`？
