package com.javahood.admin.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryTokenStore implements TokenStore {

    private record Entry(String value, long expiresAtEpochSeconds) {
        boolean isExpired() {
            return Instant.now().getEpochSecond() > expiresAtEpochSeconds;
        }
    }

    private final Map<String, Entry> refreshTokens = new ConcurrentHashMap<>();
    private final Map<String, Long> revokedAccessTokens = new ConcurrentHashMap<>();

    @Override
    public void allowRefreshToken(String jti, String username, long ttlSeconds) {
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        refreshTokens.put(jti, new Entry(username, exp));
    }

    @Override
    public boolean consumeRefreshToken(String jti, String username) {
        Entry removed = refreshTokens.remove(jti);
        return removed != null && !removed.isExpired() && username.equals(removed.value());
    }

    @Override
    public void revokeAccessToken(String jti, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            return;
        }
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        revokedAccessTokens.put(jti, exp);
    }

    @Override
    public boolean isAccessTokenRevoked(String jti) {
        Long exp = revokedAccessTokens.get(jti);
        if (exp == null) {
            return false;
        }
        if (Instant.now().getEpochSecond() > exp) {
            revokedAccessTokens.remove(jti);
            return false;
        }
        return true;
    }
}
