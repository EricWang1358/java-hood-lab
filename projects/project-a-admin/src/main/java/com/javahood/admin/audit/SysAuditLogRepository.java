package com.javahood.admin.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysAuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop50ByOrderByIdDesc();
}
