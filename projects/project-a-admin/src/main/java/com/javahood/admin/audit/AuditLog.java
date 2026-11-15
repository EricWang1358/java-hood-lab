package com.javahood.admin.audit;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "sys_audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String operator;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(nullable = false, length = 120)
    private String methodName;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected AuditLog() {
    }

    public AuditLog(String operator, String action, String methodName, boolean success, long durationMs) {
        this.operator = operator;
        this.action = action;
        this.methodName = methodName;
        this.success = success;
        this.durationMs = durationMs;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getOperator() {
        return operator;
    }

    public String getAction() {
        return action;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
