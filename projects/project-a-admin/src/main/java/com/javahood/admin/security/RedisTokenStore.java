package com.javahood.admin.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
public class RedisTokenStore implements TokenStore {

    private static final String REFRESH_PREFIX = "auth:refresh:";
    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public RedisTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void allowRefreshToken(String jti, String username, long ttlSeconds) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + jti, username, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean consumeRefreshToken(String jti, String username) {
        String key = REFRESH_PREFIX + jti;
        String storedUser = redisTemplate.opsForValue().getAndDelete(key);
        return username.equals(storedUser);
    }

    @Override
    public void revokeAccessToken(String jti, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "1", Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean isAccessTokenRevoked(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }
}
