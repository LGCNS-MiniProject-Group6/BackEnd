package com.miniproject1.miniproject1.auth.service.login;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.commons.token.JwtTokenProvider;
import com.miniproject1.miniproject1.auth.dto.login.LoginRequest;
import com.miniproject1.miniproject1.auth.dto.login.LoginResponse;
import com.miniproject1.miniproject1.auth.repository.RefreshTokenRepository;
import com.miniproject1.miniproject1.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final String dummyPasswordHash;

    public LoginService(UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public LoginResponse login(LoginRequest request) {
        var user = userRepository.findById(request.email());
        String storedPassword = user.map(value -> value.getPassword()).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), storedPassword);

        if (user.isEmpty() || !passwordMatches) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        String email = user.get().getEmail();
        String accessToken = jwtTokenProvider.createAccessToken(email, "USER");
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        refreshTokenRepository.save(
                refreshToken,
                email,
                jwtTokenProvider.getRefreshTokenExpiresIn()
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpiresIn(),
                jwtTokenProvider.getRefreshTokenExpiresIn()
        );
    }
}
