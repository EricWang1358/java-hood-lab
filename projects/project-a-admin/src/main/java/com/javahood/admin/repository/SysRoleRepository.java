package com.javahood.admin.repository;

import com.javahood.admin.domain.SysRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    @Override
    @EntityGraph(attributePaths = {"permissions"})
    List<SysRole> findAll();

    Optional<SysRole> findByCode(String code);
}
