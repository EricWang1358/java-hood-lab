package com.javahood.admin.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Foundation note (Module 5: spring-tx-prop & spring-tx-self):
 * Must be an INDEPENDENT Spring Bean with Propagation.REQUIRES_NEW.
 * 1) Why REQUIRES_NEW? When an @Audited @Transactional business method throws an exception,
 *    its outer transaction is marked rollback-only. If audit saving used default REQUIRED,
 *    the failure audit log would roll back together with the business data and vanish!
 *    REQUIRES_NEW suspends the outer tx, opens a fresh JDBC connection, commits the failure log,
 *    and then resumes the outer rollback.
 * 2) Why a separate Service Bean? Calling this.saveInNewTx() inside the same class would bypass
 *    Spring's AOP proxy (self-invocation bug), rendering @Transactional(REQUIRES_NEW) useless!
 */
@Service
public class AuditLogPersistenceService {

    private final SysAuditLogRepository auditLogRepository;

    public AuditLogPersistenceService(SysAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog saveInNewTransaction(AuditLog log) {
        return auditLogRepository.save(log);
    }
}
