package com.javahood.admin.drills;

import com.javahood.admin.audit.AuditLog;
import com.javahood.admin.audit.Audited;
import com.javahood.admin.audit.SysAuditLogRepository;
import com.javahood.admin.common.BizException;
import com.javahood.admin.common.ErrorCode;
import com.javahood.admin.common.PageResult;
import com.javahood.admin.common.TraceIdFilter;
import com.javahood.admin.domain.SysPermission;
import com.javahood.admin.repository.SysPermissionRepository;
import com.javahood.admin.security.JwtService;
import com.javahood.admin.security.LoginAttemptGuard;
import com.javahood.admin.service.AuthService;
import com.javahood.admin.service.UserService;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 项目 A 基础夯实演练（5 个可打断点的「踩坑 vs 修复」对照实验）
 *
 * 如果你看成品代码觉得「心里发虚、基础不牢」，请直接运行本测试类并顺着注释打断点：
 * 每一个测试都把【新手错误写法】与【生产正确写法】放在同一个 JVM 里真刀真枪跑出差异。
 */
@SpringBootTest
@AutoConfigureMockMvc
class FoundationPitfallDrillsTest {

    @TestConfiguration
    static class DrillProbeConfig {
        @Bean
        TransactionalFailingProbe transactionalFailingProbe(SysPermissionRepository permissionRepository) {
            return new TransactionalFailingProbe(permissionRepository);
        }
    }

    static class TransactionalFailingProbe {
        private final SysPermissionRepository permissionRepository;

        TransactionalFailingProbe(SysPermissionRepository permissionRepository) {
            this.permissionRepository = permissionRepository;
        }

        @Transactional
        @Audited(action = "DRILL_TX_FAIL_PROBE")
        public void writeThenExplode(String tempCode) {
            permissionRepository.save(new SysPermission(tempCode, "Should Rollback"));
            throw new BizException(ErrorCode.CONFLICT, "Simulated downstream failure after DB insert");
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LoginAttemptGuard loginAttemptGuard;

    @Autowired
    private UserService userService;

    @Autowired
    private SysPermissionRepository permissionRepository;

    @Autowired
    private SysAuditLogRepository auditLogRepository;

    @Autowired
    private TransactionalFailingProbe failingProbe;

    /**
     * 演练 01：Tomcat 线程池复用下的 ThreadLocal / MDC 串号事故（对应模块 0 第 18 课）
     * - 【基础盲区】：以为每个 HTTP 请求都是新线程，往 MDC/ThreadLocal 塞了 traceId 不清理。
     * - 【机盖底下】：Tomcat 工作线程是池化复用的！如果 Filter 没在 finally 里 remove，下一个请求会读到上一个人的脏上下文。
     */
    @Test
    void drill01_threadLocalMdc_leakWithoutFinally_vs_safeTraceIdFilter() throws Exception {
        ExecutorService singleWorkerPool = Executors.newSingleThreadExecutor();
        try {
            // 1) 错误示范：请求 A 放了 traceId 但忘记在 finally 中清理
            singleWorkerPool.submit(() -> MDC.put(TraceIdFilter.MDC_TRACE_KEY, "LEAKED-TRACE-A")).get();

            // 同一工作线程处理请求 B 时，竟然读到了请求 A 的 traceId（串号！）
            String leakedInRequestB = singleWorkerPool.submit(() -> MDC.get(TraceIdFilter.MDC_TRACE_KEY)).get();
            assertThat(leakedInRequestB).isEqualTo("LEAKED-TRACE-A");

            // 2) 正确示范：始终在 try-finally 中清理
            singleWorkerPool.submit(() -> {
                try {
                    MDC.put(TraceIdFilter.MDC_TRACE_KEY, "SAFE-TRACE");
                } finally {
                    MDC.remove(TraceIdFilter.MDC_TRACE_KEY);
                }
            }).get();
            String afterSafeCleanup = singleWorkerPool.submit(() -> MDC.get(TraceIdFilter.MDC_TRACE_KEY)).get();
            assertThat(afterSafeCleanup).isNull();
        } finally {
            singleWorkerPool.shutdown();
        }

        // 3) 验证本工程 TraceIdFilter：透传 X-Trace-Id 到响应头与 ApiResponse.traceId，且请求结束后主线程 MDC 已清空
        mockMvc.perform(post("/api/auth/login")
                        .header(TraceIdFilter.TRACE_ID_HEADER, "req-trace-001")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.TRACE_ID_HEADER, "req-trace-001"))
                .andExpect(jsonPath("$.traceId").value("req-trace-001"));
        assertThat(MDC.get(TraceIdFilter.MDC_TRACE_KEY)).isNull();
    }

    /**
     * 演练 02：JWT 双 Token 类型混淆攻击（为什么验签通过不等于能放行？）
     * - 【基础盲区】：以为只要 Jwts.parser().verifyWith(secretKey) 不抛异常，这个 Token 就是合法的登录凭证。
     * - 【机盖底下】：Access Token（15 分钟）和 Refresh Token（7 天）通常共用同一个 HMAC 密钥签名。
     *   如果不校验 payload 里的 type 字段，攻击者拿到 7 天的 Refresh Token 就能直接当 Access Token 调业务接口！
     */
    @Test
    void drill02_jwtTokenTypeConfusion_refreshTokenCannotSpoofAccessToken() {
        JwtService.GeneratedRefreshToken refresh = jwtService.generateRefreshToken("admin");

        // 拿合法签名的 Refresh Token 冒充 Access Token 解析 -> 必须被 type 校验当场拦截
        assertThatThrownBy(() -> jwtService.parseAndValidate(refresh.token(), JwtService.TokenType.ACCESS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unexpected token type: REFRESH");
    }

    /**
     * 演练 03：密码暴力破解防护与 ConcurrentHashMap.compute 原子计数（对应模块 0 第 3 课）
     * - 【基础盲区】：登录接口不设防，或者用 map.get() + 1 再 map.put() 统计失败次数导致并发漏记。
     * - 【机盖底下】：LoginAttemptGuard 用 compute() 在桶锁内原子累加，连续错 5 次立即锁定（42901）。
     */
    @Test
    void drill03_loginBruteForceGuard_locksAfterFiveFailures() {
        String targetUser = "viewer";
        loginAttemptGuard.reset(targetUser);

        for (int i = 0; i < LoginAttemptGuard.MAX_FAILURES; i++) {
            final int attempt = i;
            assertThatThrownBy(() -> authService.login(targetUser, "wrong-pass-" + attempt))
                    .isInstanceOf(BadCredentialsException.class);
        }

        // 第 6 次尝试：即使输对密码，在锁定窗口内也会直接被防爆破拦截（不再打库做 BCrypt 哈希消耗 CPU）
        assertThatThrownBy(() -> authService.login(targetUser, "view123"))
                .isInstanceOf(BizException.class)
                .extracting(ex -> ((BizException) ex).getErrorCode())
                .isEqualTo(ErrorCode.TOO_MANY_REQUESTS);

        // 清理测试状态，避免影响其他测试
        loginAttemptGuard.reset(targetUser);
    }

    /**
     * 演练 04：Spring 事务传播机制 REQUIRES_NEW 实战（对应模块 5 spring-tx-prop & spring-tx-self）
     * - 【基础盲区】：在 @Around 切面里直接调 repository.save(auditLog)，结果业务方法一抛异常，失败审计日志跟着一起回滚没了！
     * - 【机盖底下】：AuditLogPersistenceService 拆成独立 Bean 并声明 Propagation.REQUIRES_NEW，
     *   挂起外层即将回滚的事务，借用新连接把 success=false 的案发现场独立提交落盘。
     */
    @Test
    void drill04_auditLogRequiresNew_survivesOuterTransactionRollback() {
        String tempPermCode = "perm:rollback_probe";
        MDC.put(TraceIdFilter.MDC_TRACE_KEY, "trace-tx-rollback-99");
        try {
            assertThatThrownBy(() -> failingProbe.writeThenExplode(tempPermCode))
                    .isInstanceOf(BizException.class);
        } finally {
            MDC.remove(TraceIdFilter.MDC_TRACE_KEY);
        }

        // 1) 业务数据必须已彻底回滚（数据库里绝不能留下半截脏权限）
        assertThat(permissionRepository.findByCode(tempPermCode)).isEmpty();

        // 2) 但失败审计日志必须靠 REQUIRES_NEW 独立存活下来，且准确记录了 traceId 与报错原因！
        List<AuditLog> logs = auditLogRepository.findTop50ByOrderByIdDesc();
        AuditLog failureLog = logs.stream()
                .filter(l -> "DRILL_TX_FAIL_PROBE".equals(l.getAction()))
                .findFirst()
                .orElseThrow();

        assertThat(failureLog.isSuccess()).isFalse();
        assertThat(failureLog.getTraceId()).isEqualTo("trace-tx-rollback-99");
        assertThat(failureLog.getErrorMessage()).contains("Simulated downstream failure");
    }

    /**
     * 演练 05：关掉 OSIV (open-in-view: false) + LAZY + @BatchSize，既无 LazyInitializationException 也无 N+1
     * - 【基础盲区】：新手怕报 LazyInitializationException 就把 @ManyToMany 全改成 EAGER，结果分页查 20 个用户触发 20+ 次额外 SQL。
     * - 【机盖底下】：Service 层在 @Transactional(readOnly = true) 内配合 @BatchSize 批量拉取关联角色并组装成不可变 UserVO Record，
     *   出事务边界前已完成投影，既杜绝 Controller 层懒加载异常，又保住数据库 LIMIT 分页性能。
     */
    @Test
    void drill05_lazyBatchSizeWithDtoProjection_worksCleanlyWithOpenInViewFalse() {
        PageResult<UserService.UserVO> page = userService.queryUsers(null, true, 0, 10);
        assertThat(page.items()).isNotEmpty();
        assertThat(page.items())
                .anySatisfy(u -> {
                    assertThat(u.username()).isEqualTo("admin");
                    assertThat(u.roles()).contains("ROLE_ADMIN");
                });
    }
}
