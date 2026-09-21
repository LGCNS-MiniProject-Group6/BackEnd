package com.miniproject1.miniproject1.commons.token;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    @Value("${jwt.access-token-expires-in:3600}")
    private long accessTokenExpiresIn;

    @Value("${jwt.refresh-token-expires-in:1209600}")
    private long refreshTokenExpiresIn;

    public long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }

    public long getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }

    @PostConstruct
    protected void init() {
        if (accessTokenExpiresIn <= 0 || refreshTokenExpiresIn <= 0) {
            throw new IllegalArgumentException("Token lifetimes must be positive");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 1. Access Token 생성
    public String createAccessToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.put("tokenType", "access");

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenExpiresIn * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Refresh Token 생성
    public String createRefreshToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("tokenType", "refresh");

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpiresIn * 1000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parse(token);
        validateTokenType(claims, "access");
        return claims;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = parse(token);
        validateTokenType(claims, "refresh");
        return claims;
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private void validateTokenType(Claims claims, String expectedType) {
        if (!expectedType.equals(claims.get("tokenType", String.class))
                || claims.getSubject() == null
                || claims.getSubject().isBlank()
                || claims.getExpiration() == null) {
            throw new JwtException("Invalid " + expectedType + " token");
        }
    }

    // 3. 토큰에서 사용자 이메일 추출
    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 4. 토큰 유효성 및 만료 여부 확인
    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
