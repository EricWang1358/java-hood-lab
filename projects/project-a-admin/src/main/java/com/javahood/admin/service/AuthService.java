package com.javahood.admin.service;

import com.javahood.admin.common.BizException;
import com.javahood.admin.common.ErrorCode;
import com.javahood.admin.security.AdminUserDetailsService;
import com.javahood.admin.security.JwtProperties;
import com.javahood.admin.security.JwtService;
import com.javahood.admin.security.TokenStore;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    public record TokenPair(String accessToken, String refreshToken, long accessExpiresInSeconds) {
    }

    private final AuthenticationManager authenticationManager;
    private final AdminUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final TokenStore tokenStore;
    private final JwtProperties jwtProperties;

    public AuthService(AuthenticationManager authenticationManager,
                       AdminUserDetailsService userDetailsService,
                       JwtService jwtService,
                       TokenStore tokenStore,
                       JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.tokenStore = tokenStore;
        this.jwtProperties = jwtProperties;
    }

    public TokenPair login(String username, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return issueTokenPair(username, authorities);
    }

    public TokenPair refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parseAndValidate(refreshToken, JwtService.TokenType.REFRESH);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BizException(ErrorCode.TOKEN_REVOKED, "Invalid refresh token");
        }

        String jti = claims.getId();
        String username = claims.getSubject();
        if (jti == null || !tokenStore.consumeRefreshToken(jti, username)) {
            throw new BizException(ErrorCode.TOKEN_REVOKED, "Refresh token already used or revoked");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!userDetails.isEnabled()) {
            throw new BizException(ErrorCode.FORBIDDEN, "User account is disabled");
        }
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return issueTokenPair(username, authorities);
    }

    public void logout(String bearerHeader, String refreshToken) {
        if (bearerHeader != null && bearerHeader.startsWith("Bearer ")) {
            String accessToken = bearerHeader.substring(7);
            try {
                Claims claims = jwtService.parseAndValidate(accessToken, JwtService.TokenType.ACCESS);
                if (claims.getId() != null) {
                    tokenStore.revokeAccessToken(claims.getId(), jwtService.remainingSeconds(claims));
                }
            } catch (JwtException | IllegalArgumentException ignored) {
            }
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                Claims claims = jwtService.parseAndValidate(refreshToken, JwtService.TokenType.REFRESH);
                if (claims.getId() != null) {
                    tokenStore.consumeRefreshToken(claims.getId(), claims.getSubject());
                }
            } catch (JwtException | IllegalArgumentException ignored) {
            }
        }
    }

    private TokenPair issueTokenPair(String username, List<String> authorities) {
        String accessToken = jwtService.generateAccessToken(username, authorities);
        JwtService.GeneratedRefreshToken refresh = jwtService.generateRefreshToken(username);
        tokenStore.allowRefreshToken(refresh.jti(), username, refresh.ttlSeconds());
        return new TokenPair(accessToken, refresh.token(), jwtProperties.accessTtlSeconds());
    }
}
