package com.miniproject1.miniproject1;

import com.miniproject1.miniproject1.auth.service.SmsService;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.commons.token.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@RestController
// @RequestMapping 지우거나 비워두기!
public class TestController {

    private final SmsService smsService;
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

    @GetMapping("/test/error")
    public void testError() {
        throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "사업정보를 찾을 수 없습니다.");
    }

    @Operation(summary = "3. SMS 인증번호 발송 테스트")
    @PostMapping("/test/sms/send")
    public ResponseEntity<?> testSmsSend(@RequestParam String phoneNumber) {
        smsService.sendVerificationCode(phoneNumber);
        return ResponseEntity.ok(Map.of("message", "인증번호 발송 성공 (Redis 저장 완료)"));
    }

    @Operation(summary = "4. SMS 인증번호 검증 테스트")
    @PostMapping("/test/sms/verify")
    public ResponseEntity<?> testSmsVerify(@RequestParam String phoneNumber, @RequestParam String code) {
        boolean result = smsService.verifyCode(phoneNumber, code);
        return ResponseEntity.ok(Map.of(
                "message", "인증 성공!",
                "isVerified", result));
    }
}