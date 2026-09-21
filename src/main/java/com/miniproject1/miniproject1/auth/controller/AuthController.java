package com.miniproject1.miniproject1.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.miniproject1.miniproject1.auth.dto.Token.TokenRequestDTO;
import com.miniproject1.miniproject1.auth.dto.Token.TokenResponseDTO;
import com.miniproject1.miniproject1.auth.dto.login.LoginRequest;
import com.miniproject1.miniproject1.auth.dto.login.LoginResponse;
import com.miniproject1.miniproject1.auth.dto.logout.LogoutRequest;
import com.miniproject1.miniproject1.auth.service.sms.SmsService;
import com.miniproject1.miniproject1.auth.service.token.TokenService;
import com.miniproject1.miniproject1.auth.service.login.LoginService;
import com.miniproject1.miniproject1.auth.service.logout.LogoutService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "인증 / 로그인 / 토큰 관리 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final LogoutService logoutService;
    private final TokenService tokenService;
    private final SmsService smsService;

    @Operation(summary = "1. 이메일과 비밀번호로 로그인")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "2. Refresh Token 폐기 및 로그아웃")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        logoutService.logout(request);
    }

    @Operation(summary = "3. 리프레시 토큰 재발급(RTR)")
    @PostMapping("/auth/reissue")
    public ResponseEntity<TokenResponseDTO> reissue(@Valid @RequestBody TokenRequestDTO request) {
        TokenResponseDTO response = tokenService.reissue(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "4. SMS 인증번호 발송 테스트")
    @PostMapping("/phone-verification/send")
    public ResponseEntity<?> testSmsSend(@RequestParam String phoneNumber) {
        smsService.sendVerificationCode(phoneNumber);
        return ResponseEntity.ok(Map.of("message", "인증번호 발송 성공 (Redis 저장 완료)"));
    }

    @Operation(summary = "5. SMS 인증번호 검증 테스트")
    @PostMapping("/phone-verification/verify")
    public ResponseEntity<?> testSmsVerify(@RequestParam String phoneNumber, @RequestParam String code) {
        boolean result = smsService.verifyCode(phoneNumber, code);
        return ResponseEntity.ok(Map.of(
                "message", "인증 성공!",
                "isVerified", result));
    }
}