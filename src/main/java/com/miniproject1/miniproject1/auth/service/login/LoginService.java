package com.miniproject1.miniproject1.auth.service.login;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.miniproject1.miniproject1.auth.dto.login.LoginRequest;
import com.miniproject1.miniproject1.auth.dto.login.LoginResponse;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.commons.token.JwtTokenProvider;
import com.miniproject1.miniproject1.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에서 주입받은 PasswordEncoder
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        String dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());

        var user = userRepository.findById(request.email());
        String storedPassword = user.map(value -> value.getPassword()).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), storedPassword);

        if (user.isEmpty() || !passwordMatches) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        String email = user.get().getEmail();

        // 1. role 없이 email만 사용하여 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        // 2. Redis DB에 직접 저장 ("RT:email" 키 구조)
        long refreshTokenExpiration = jwtTokenProvider.getRefreshTokenExpirationTime();
        redisTemplate.opsForValue().set(
                "RT:" + email,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS);

        // 3. 응답 반환
        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationTime(),
                refreshTokenExpiration);
    }
}