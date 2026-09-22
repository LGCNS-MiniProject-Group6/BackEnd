package com.miniproject1.miniproject1.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.miniproject1.miniproject1.auth.dto.signup.request.SignupRequestDTO;
import com.miniproject1.miniproject1.auth.dto.signup.response.EmailCheckResponseDTO;
import com.miniproject1.miniproject1.auth.dto.signup.response.SignupResponseDTO;
import com.miniproject1.miniproject1.auth.service.login.LoginService;
import com.miniproject1.miniproject1.auth.service.logout.LogoutService;
import com.miniproject1.miniproject1.auth.service.signup.EmailCheckService;
import com.miniproject1.miniproject1.auth.service.signup.SignupService;
import com.miniproject1.miniproject1.auth.service.sms.SmsService;
import com.miniproject1.miniproject1.auth.service.token.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "인증 / 로그인 / 토큰 관리 API")
@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final EmailCheckService emailCheckService;
    private final SmsService smsService;
    private final SignupService signupService;
    private final LoginService loginService;
    private final TokenService tokenService;
    private final LogoutService logoutService;

    // --- [ 1. 회원가입 사전 인증단계 ] ---

    @Operation(summary = "1. 이메일 중복확인")
    @GetMapping("/check-email")
    public ResponseEntity<EmailCheckResponseDTO> checkEmail(
            @RequestParam("email") @NotBlank(message = "이메일은 필수입니다.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email) {
        EmailCheckResponseDTO response = emailCheckService.checkEmail(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "2. SMS 인증번호 발송")
    @PostMapping("/phone-verification/send")
    public ResponseEntity<?> sendSmsCode(@RequestParam("phoneNumber") String phoneNumber) {
        smsService.sendVerificationCode(phoneNumber);
        return ResponseEntity.ok(Map.of("message", "인증번호 발송 성공 (Redis 저장 완료)"));
    }

    @Operation(summary = "3. SMS 인증번호 검증")
    @PostMapping("/phone-verification/verify")
    public ResponseEntity<?> verifySmsCode(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("code") String code) {
        boolean result = smsService.verifyCode(phoneNumber, code);
        return ResponseEntity.ok(Map.of(
                "message", "인증 성공!",
                "isVerified", result));
    }

    @Operation(summary = "4. 회원가입")
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        SignupResponseDTO response = signupService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- [ 2. 로그인 및 토큰 / 세션 관리단계 ] ---

    @Operation(summary = "5. 이메일과 비밀번호로 로그인")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = loginService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "6. 리프레시 토큰 재발급(RTR)")
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDTO> reissue(@Valid @RequestBody TokenRequestDTO request) {
        TokenResponseDTO response = tokenService.reissue(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "7. Refresh Token 폐기 및 로그아웃")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest request) {
        logoutService.logout(request);
    }
}