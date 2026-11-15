package com.javahood.admin.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

    private final SysAuditLogRepository auditLogRepository;

    public AuditLogAspect(SysAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("@annotation(audited)")
    public Object around(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        long startNs = System.nanoTime();
        boolean success = true;
        try {
            return pjp.proceed();
        } catch (Throwable ex) {
            success = false;
            throw ex;
        } finally {
            long durationMs = (System.nanoTime() - startNs) / 1_000_000L;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String operator = (auth != null && auth.getName() != null) ? auth.getName() : "anonymous";
            String methodName = pjp.getSignature().getDeclaringType().getSimpleName() + "." + pjp.getSignature().getName();
            auditLogRepository.save(new AuditLog(operator, audited.action(), methodName, success, durationMs));
        }
    }
}
