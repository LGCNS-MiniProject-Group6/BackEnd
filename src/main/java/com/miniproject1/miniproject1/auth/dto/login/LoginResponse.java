package com.miniproject1.miniproject1.auth.dto.login;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
}
