package com.javahood.admin.security;

import com.javahood.admin.domain.SysPermission;
import com.javahood.admin.domain.SysRole;
import com.javahood.admin.domain.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AdminUserDetails implements UserDetails {

    private final SysUser user;
    private final Set<GrantedAuthority> authorities;

    public AdminUserDetails(SysUser user) {
        this.user = user;
        this.authorities = new HashSet<>();
        for (SysRole role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getCode()));
            for (SysPermission perm : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(perm.getCode()));
            }
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
