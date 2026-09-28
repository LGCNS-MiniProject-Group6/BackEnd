package com.miniproject1.miniproject1.auth.service.logout;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.miniproject1.miniproject1.auth.dto.logout.LogoutRequest;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.commons.token.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    public void logout(LogoutRequest request) {
        String refreshToken = request.refreshToken();

        // 1. Refresh Token 검증 (유효하지 않거나 만료된 토큰일 경우)
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 2. 토큰에서 사용자 이메일 추출
        String email = jwtTokenProvider.getEmail(refreshToken);

        // 3. Redis DB에서 해당 이메일("RT:email") 키로 저장된 토큰 조회
        String storedRefreshToken = redisTemplate.opsForValue().get("RT:" + email);

        // 이미 로그아웃 처리되었거나 Redis에서 만료되어 삭제된 경우 (정상 종료 처리)
        if (storedRefreshToken == null) {
            return;
        }

        // 4. Redis DB에 저장된 토큰과 클라이언트가 보낸 토큰이 일치하지 않는 경우
        if (!storedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 5. Redis DB에서 해당 Refresh Token 삭제
        redisTemplate.delete("RT:" + email);
    }
}
