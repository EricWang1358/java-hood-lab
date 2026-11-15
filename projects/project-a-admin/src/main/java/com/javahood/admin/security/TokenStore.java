package com.javahood.admin.security;

public interface TokenStore {
    void allowRefreshToken(String jti, String username, long ttlSeconds);

    boolean consumeRefreshToken(String jti, String username);

    void revokeAccessToken(String jti, long ttlSeconds);

    boolean isAccessTokenRevoked(String jti);
}
