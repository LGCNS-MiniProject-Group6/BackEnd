package com.miniproject1.miniproject1.auth.service.logout;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.commons.token.JwtTokenProvider;
import com.miniproject1.miniproject1.auth.dto.logout.LogoutRequest;
import com.miniproject1.miniproject1.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public void logout(LogoutRequest request) {
        String email;
        try {
            email = jwtTokenProvider.parseRefreshToken(request.refreshToken()).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        var storedEmail = refreshTokenRepository.findEmail(request.refreshToken());
        if (storedEmail.isEmpty()) {
            return;
        }
        if (!storedEmail.get().equals(email)) {
            throw new BusinessException(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        refreshTokenRepository.delete(request.refreshToken());
    }
}
