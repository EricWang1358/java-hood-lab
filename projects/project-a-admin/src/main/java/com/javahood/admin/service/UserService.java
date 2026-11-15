package com.javahood.admin.service;

import com.javahood.admin.common.BizException;
import com.javahood.admin.common.ErrorCode;
import com.javahood.admin.common.PageResult;
import com.javahood.admin.domain.SysRole;
import com.javahood.admin.domain.SysUser;
import com.javahood.admin.repository.SysRoleRepository;
import com.javahood.admin.repository.SysUserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    public record UserVO(
            Long id,
            String username,
            String displayName,
            String email,
            boolean enabled,
            Set<String> roles,
            Instant createdAt
    ) {
        public static UserVO from(SysUser u) {
            return new UserVO(
                    u.getId(),
                    u.getUsername(),
                    u.getDisplayName(),
                    u.getEmail(),
                    u.isEnabled(),
                    u.getRoles().stream().map(SysRole::getCode).collect(Collectors.toSet()),
                    u.getCreatedAt()
            );
        }
    }

    public record CreateUserCmd(
            String username,
            String password,
            String displayName,
            String email,
            Set<String> roleCodes
    ) {
    }

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(SysUserRepository userRepository,
                       SysRoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResult<UserVO> queryUsers(String keyword, Boolean enabled, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Specification<SysUser> spec = buildSpec(keyword, enabled);
        Page<UserVO> result = userRepository
                .findAll(spec, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id")))
                .map(UserVO::from);
        return PageResult.from(result);
    }

    @Transactional
    public UserVO createUser(CreateUserCmd cmd) {
        if (userRepository.existsByUsername(cmd.username())) {
            throw new BizException(ErrorCode.CONFLICT, "Username already exists: " + cmd.username());
        }
        Set<SysRole> roles = new HashSet<>();
        if (cmd.roleCodes() != null) {
            for (String code : cmd.roleCodes()) {
                SysRole role = roleRepository.findByCode(code)
                        .orElseThrow(() -> new BizException(ErrorCode.BAD_REQUEST, "Unknown role: " + code));
                roles.add(role);
            }
        }
        SysUser user = new SysUser(
                cmd.username(),
                passwordEncoder.encode(cmd.password()),
                cmd.displayName(),
                cmd.email(),
                roles
        );
        return UserVO.from(userRepository.save(user));
    }

    @Transactional
    public UserVO updateStatus(Long id, boolean enabled) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "User not found: " + id));
        user.setEnabled(enabled);
        return UserVO.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public void exportCsv(String keyword, Boolean enabled, Writer writer) throws IOException {
        // UTF-8 BOM for Excel compatibility
        writer.write('\uFEFF');
        writer.write("ID,Username,DisplayName,Email,Enabled,Roles,CreatedAt\r\n");

        Specification<SysUser> spec = buildSpec(keyword, enabled);
        int page = 0;
        Page<SysUser> slice;
        do {
            slice = userRepository.findAll(spec, PageRequest.of(page++, 200, Sort.by("id")));
            for (SysUser u : slice.getContent()) {
                String roles = u.getRoles().stream().map(SysRole::getCode).sorted().collect(Collectors.joining("|"));
                writer.write(String.join(",",
                        String.valueOf(u.getId()),
                        csvCell(u.getUsername()),
                        csvCell(u.getDisplayName()),
                        csvCell(u.getEmail()),
                        String.valueOf(u.isEnabled()),
                        csvCell(roles),
                        csvCell(u.getCreatedAt().toString())
                ));
                writer.write("\r\n");
            }
        } while (slice.hasNext());
        writer.flush();
    }

    private Specification<SysUser> buildSpec(String keyword, Boolean enabled) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("displayName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                ));
            }
            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Escapes CSV quotes and neutralizes spreadsheet formula injection (=, +, -, @).
     */
    static String csvCell(String raw) {
        if (raw == null) {
            return "";
        }
        String safe = raw;
        if (!safe.isEmpty()) {
            char first = safe.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@') {
                safe = "'" + safe;
            }
        }
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }
}
