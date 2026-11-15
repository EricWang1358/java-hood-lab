package com.javahood.admin.audit;

import com.javahood.admin.common.TraceIdFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect ordering note (Module 5: spring-aop-order):
 * Ordered BEFORE default @Transactional interceptor (Ordered.LOWEST_PRECEDENCE) or uses REQUIRES_NEW
 * via AuditLogPersistenceService so that even when an inner @Transactional method throws an exception,
 * the failure audit record is durably committed.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuditLogAspect {

    private final AuditLogPersistenceService persistenceService;

    public AuditLogAspect(AuditLogPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    @Around("@annotation(audited)")
    public Object around(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        long startNs = System.nanoTime();
        boolean success = true;
        String errorMsg = null;
        try {
            return pjp.proceed();
        } catch (Throwable ex) {
            success = false;
            String raw = ex.getMessage();
            errorMsg = (raw == null) ? ex.getClass().getSimpleName()
                    : (raw.length() > 240 ? raw.substring(0, 240) : raw);
            throw ex;
        } finally {
            long durationMs = (System.nanoTime() - startNs) / 1_000_000L;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String operator = (auth != null && auth.getName() != null) ? auth.getName() : "anonymous";
            String methodName = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
            String traceId = MDC.get(TraceIdFilter.MDC_TRACE_KEY);
            persistenceService.saveInNewTransaction(
                    new AuditLog(operator, audited.action(), methodName, success, durationMs, traceId, errorMsg)
            );
        }
    }
}
