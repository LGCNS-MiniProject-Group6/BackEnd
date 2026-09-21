package com.miniproject1.miniproject1.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    public void save(String token, String email, long expiresInSeconds) {
        redisTemplate.opsForValue().set(key(token), email, Duration.ofSeconds(expiresInSeconds));
    }

    public Optional<String> findEmail(String token) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(token)));
    }

    public void delete(String token) {
        redisTemplate.delete(key(token));
    }

    private String key(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
