package com.javahood.admin.repository;

import com.javahood.admin.domain.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {
    Optional<SysPermission> findByCode(String code);
}
