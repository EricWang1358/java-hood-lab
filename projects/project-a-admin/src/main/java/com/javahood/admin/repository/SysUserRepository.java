package com.javahood.admin.repository;

import com.javahood.admin.domain.SysUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

    /**
     * Single-query LEFT JOIN FETCH via @EntityGraph for authentication lookup (1 user -> roles -> permissions).
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<SysUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
