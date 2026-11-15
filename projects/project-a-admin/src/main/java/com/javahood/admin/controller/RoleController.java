package com.javahood.admin.controller;

import com.javahood.admin.audit.AuditLog;
import com.javahood.admin.audit.SysAuditLogRepository;
import com.javahood.admin.common.ApiResponse;
import com.javahood.admin.domain.SysPermission;
import com.javahood.admin.repository.SysRoleRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RoleController {

    public record RoleVO(Long id, String code, String name, Set<String> permissions) {
    }

    private final SysRoleRepository roleRepository;
    private final SysAuditLogRepository auditLogRepository;

    public RoleController(SysRoleRepository roleRepository, SysAuditLogRepository auditLogRepository) {
        this.roleRepository = roleRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('user:read')")
    public ApiResponse<List<RoleVO>> listRoles() {
        List<RoleVO> roles = roleRepository.findAll().stream()
                .map(r -> new RoleVO(
                        r.getId(),
                        r.getCode(),
                        r.getName(),
                        r.getPermissions().stream().map(SysPermission::getCode).collect(Collectors.toSet())
                ))
                .toList();
        return ApiResponse.ok(roles);
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('user:write')")
    public ApiResponse<List<AuditLog>> recentAuditLogs() {
        return ApiResponse.ok(auditLogRepository.findTop50ByOrderByIdDesc());
    }
}
