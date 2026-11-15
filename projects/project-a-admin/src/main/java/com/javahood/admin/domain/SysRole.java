package com.javahood.admin.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sys_role")
public class SysRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Foundation note: Use LAZY + @BatchSize instead of FetchType.EAGER.
     * FetchType.EAGER triggers hidden N+1 queries whenever roles are listed;
     * LAZY + @BatchSize (or @EntityGraph on targeted queries) keeps SQL predictable.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @JoinTable(
            name = "sys_role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<SysPermission> permissions = new HashSet<>();

    protected SysRole() {
    }

    public SysRole(String code, String name, Set<SysPermission> permissions) {
        this.code = code;
        this.name = name;
        this.permissions = permissions;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Set<SysPermission> getPermissions() {
        return permissions;
    }
}
