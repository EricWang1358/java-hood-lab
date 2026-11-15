package com.javahood.admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    public enum TokenType {
        ACCESS, REFRESH
    }

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    public String generateAccessToken(String username, List<String> authorities) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.accessTtlSeconds());
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("type", TokenType.ACCESS.name())
                .claim("auth", authorities)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey)
                .compact();
    }

    public GeneratedRefreshToken generateRefreshToken(String username) {
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.refreshTtlSeconds());
        String token = Jwts.builder()
                .id(jti)
                .subject(username)
                .claim("type", TokenType.REFRESH.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey)
                .compact();
        return new GeneratedRefreshToken(jti, token, properties.refreshTtlSeconds());
    }

    public Claims parseAndValidate(String token, TokenType expectedType) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String type = claims.get("type", String.class);
        if (!expectedType.name().equals(type)) {
            throw new IllegalArgumentException("Unexpected token type: " + type);
        }
        return claims;
    }

    public long remainingSeconds(Claims claims) {
        long expSec = claims.getExpiration().toInstant().getEpochSecond();
        return Math.max(0, expSec - Instant.now().getEpochSecond());
    }

    public record GeneratedRefreshToken(String jti, String token, long ttlSeconds) {
    }
}
