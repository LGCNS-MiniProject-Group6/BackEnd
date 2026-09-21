package com.miniproject1.miniproject1.auth.service.token;

import com.miniproject1.miniproject1.auth.dto.Token.TokenRequestDTO;
import com.miniproject1.miniproject1.auth.dto.Token.TokenResponseDTO;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.commons.token.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

        private final JwtTokenProvider jwtTokenProvider;
        private final StringRedisTemplate redisTemplate;

        /**
         * AUTH-07: 토큰 재발급
         * RTR(Refresh Token Rotation) 방식
         */
        @Transactional
        public TokenResponseDTO reissue(TokenRequestDTO request) {

                // 1. 클라이언트가 보낸 Refresh Token
                String refreshToken = request.getRefreshToken();

                // 2. Refresh Token 자체 검증
                if (!jwtTokenProvider.validateToken(refreshToken)) {
                        throw new BusinessException(
                                        ErrorCode.INVALID_TOKEN,
                                        "유효하지 않거나 만료된 Refresh Token입니다.");
                }

                // 3. Refresh Token에서 email 추출
                String email = jwtTokenProvider.getEmail(refreshToken);

                // 4. Redis에 저장된 Refresh Token 조회
                String redisKey = "RT:" + email;
                String savedRefreshToken = redisTemplate.opsForValue().get(redisKey);

                // 5. Redis에 없거나 기존 토큰과 다르면 재사용으로 판단
                if (savedRefreshToken == null
                                || !savedRefreshToken.equals(refreshToken)) {

                        // 기존 Refresh Token 폐기
                        redisTemplate.delete(redisKey);

                        throw new BusinessException(
                                        ErrorCode.INVALID_TOKEN,
                                        "이미 사용되었거나 유효하지 않은 Refresh Token입니다.");
                }

                // 6. 새로운 Access Token 발급
                String newAccessToken = jwtTokenProvider.createAccessToken(email);

                // 7. 새로운 Refresh Token 발급
                String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

                // 8. Redis의 기존 Refresh Token을 새 Refresh Token으로 교체
                redisTemplate.opsForValue().set(
                                redisKey,
                                newRefreshToken,
                                jwtTokenProvider.getRefreshTokenExpirationTime(),
                                TimeUnit.MILLISECONDS);

                // 9. 새로운 토큰 2개 반환
                return TokenResponseDTO.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(newRefreshToken)
                                .build();
        }
}