package com.javahood.admin.config;

import com.javahood.admin.domain.SysPermission;
import com.javahood.admin.domain.SysRole;
import com.javahood.admin.domain.SysUser;
import com.javahood.admin.repository.SysPermissionRepository;
import com.javahood.admin.repository.SysRoleRepository;
import com.javahood.admin.repository.SysUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysPermissionRepository permissionRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysPermissionRepository permissionRepository,
                           SysRoleRepository roleRepository,
                           SysUserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        SysPermission pRead = permissionRepository.save(new SysPermission("user:read", "Read Users"));
        SysPermission pWrite = permissionRepository.save(new SysPermission("user:write", "Manage Users"));
        SysPermission pExport = permissionRepository.save(new SysPermission("user:export", "Export Users"));

        SysRole adminRole = roleRepository.save(new SysRole("ROLE_ADMIN", "Administrator", Set.of(pRead, pWrite, pExport)));
        SysRole operatorRole = roleRepository.save(new SysRole("ROLE_OPERATOR", "Operator", Set.of(pRead, pExport)));
        SysRole viewerRole = roleRepository.save(new SysRole("ROLE_VIEWER", "Viewer", Set.of(pRead)));

        userRepository.save(new SysUser("admin", passwordEncoder.encode("admin123"), "System Admin", "admin@javahood.dev", Set.of(adminRole)));
        userRepository.save(new SysUser("operator", passwordEncoder.encode("oper123"), "Ops User", "ops@javahood.dev", Set.of(operatorRole)));
        userRepository.save(new SysUser("viewer", passwordEncoder.encode("view123"), "Readonly Viewer", "viewer@javahood.dev", Set.of(viewerRole)));
    }
}
