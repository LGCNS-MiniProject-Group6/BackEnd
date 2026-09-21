package com.miniproject1.miniproject1;

import com.miniproject1.miniproject1.commons.token.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 기본 Config 및 JWT 인증 동작 검증을 위한 임시 컨트롤러
 */
@Tag(name = "00. Config Test", description = "기본 설정 및 JWT 동작 확인용 API")
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "1. 테스트용 임시 토큰 발급 (Public)")
    @GetMapping("/token")
    public ResponseEntity<?> createTestToken(@RequestParam String email, @RequestParam String role) {
        String accessToken = jwtTokenProvider.createAccessToken(email, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken));
    }

    @Operation(summary = "2. JWT 인증 필요한 보호된 API (Protected)")
    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return ResponseEntity.ok(Map.of(
                "message", "인증에 성공했습니다!",
                "userEmail", authentication.getName(),
                "authorities", authentication.getAuthorities()));
    }
}